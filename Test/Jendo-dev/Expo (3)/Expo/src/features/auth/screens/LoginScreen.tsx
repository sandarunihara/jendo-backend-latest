import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, TouchableOpacity, Alert, StyleSheet, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { GoogleSignin, statusCodes } from '@react-native-google-signin/google-signin';
import { ScreenWrapper } from '../../../common/components/layout';
import { Button, Input } from '../../../common/components/ui';
import { COLORS, TYPOGRAPHY, SPACING } from '../../../config/theme.config';
import { authStyles as styles } from '../components';
import { useAuth } from '../../../providers/AuthProvider';
import { useToast } from '../../../providers/ToastProvider';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
            auto_select?: boolean;
          }) => void;
          prompt: () => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              theme?: 'outline' | 'filled_blue' | 'filled_black';
              size?: 'large' | 'medium' | 'small';
              type?: 'standard' | 'icon';
              text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
              shape?: 'rectangular' | 'pill' | 'circle' | 'square';
              width?: number;
            }
          ) => void;
        };
      };
    };
  }
}

interface FormErrors {
  email?: string;
  password?: string;
}

const GOOGLE_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID || '';
const GOOGLE_WEB_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID || '';
const GOOGLE_ANDROID_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID || '';

export const LoginScreen: React.FC = () => {
  const router = useRouter();
  const { login, loginWithGoogle } = useAuth();
  const { showToast } = useToast();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [googleScriptLoaded, setGoogleScriptLoaded] = useState(false);
  const googleButtonRef = useRef<View>(null);

  const handleGoogleCredentialResponse = useCallback(async (response: { credential: string }) => {
    setGoogleLoading(true);
    try {
      await loginWithGoogle({ idToken: response.credential });
      showToast('Login successful!', 'success');
      router.replace('/(tabs)');
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Google login failed';
      showToast(errorMessage, 'error');
    } finally {
      setGoogleLoading(false);
    }
  }, [loginWithGoogle, router, showToast]);

  // Configure Google Sign-In for Android/iOS
  useEffect(() => {
    if (Platform.OS === 'android' || Platform.OS === 'ios') {
      GoogleSignin.configure({
        webClientId: GOOGLE_WEB_CLIENT_ID,
        offlineAccess: true,
      });
    }
  }, []);

  const handleNativeGoogleLogin = async () => {
    setGoogleLoading(true);
    try {
      await GoogleSignin.hasPlayServices();
      const userInfo = await GoogleSignin.signIn();
      
      if (userInfo.idToken) {
        await loginWithGoogle({ idToken: userInfo.idToken });
        showToast('Login successful!', 'success');
        router.replace('/(tabs)');
      } else {
        showToast('Failed to get Google ID token', 'error');
      }
    } catch (error: any) {
      if (error.code === statusCodes.SIGN_IN_CANCELLED) {
        showToast('Sign in cancelled', 'info');
      } else if (error.code === statusCodes.IN_PROGRESS) {
        showToast('Sign in already in progress', 'info');
      } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        showToast('Play services not available', 'error');
      } else {
        showToast('Google sign in failed', 'error');
        console.error('Google Sign-In Error:', error);
      }
    } finally {
      setGoogleLoading(false);
    }
  };

  useEffect(() => {
    if (Platform.OS !== 'web' || !GOOGLE_CLIENT_ID) return;

    const loadGoogleScript = () => {
      if (document.getElementById('google-gsi-script')) {
        if (window.google?.accounts?.id) {
          setGoogleScriptLoaded(true);
        }
        return;
      }

      const script = document.createElement('script');
      script.id = 'google-gsi-script';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => {
        setGoogleScriptLoaded(true);
      };
      document.head.appendChild(script);
    };

    loadGoogleScript();
  }, []);

  useEffect(() => {
    if (Platform.OS !== 'web' || !googleScriptLoaded || !GOOGLE_CLIENT_ID) return;

    const initializeGoogle = () => {
      if (window.google?.accounts?.id) {
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: handleGoogleCredentialResponse,
          auto_select: false,
        });

        const refElement = googleButtonRef.current as any;
        const buttonContainer = refElement?._nativeTag 
          ? document.querySelector(`[data-nativeid="${refElement._nativeTag}"]`)
          : (refElement as unknown as HTMLElement);
        
        if (!buttonContainer) {
          const fallbackContainer = document.querySelector('[data-google-button="true"]');
          if (fallbackContainer) {
            window.google.accounts.id.renderButton(fallbackContainer as HTMLElement, {
              theme: 'outline',
              size: 'large',
              text: 'continue_with',
              shape: 'rectangular',
              width: 300,
            });
          }
        } else {
          window.google.accounts.id.renderButton(buttonContainer as HTMLElement, {
            theme: 'outline',
            size: 'large',
            text: 'continue_with',
            shape: 'rectangular',
            width: 300,
          });
        }
      }
    };

    const timer = setTimeout(initializeGoogle, 200);
    return () => clearTimeout(timer);
  }, [googleScriptLoaded, handleGoogleCredentialResponse]);

  const validateForm = (): FormErrors => {
    const newErrors: FormErrors = {};

    if (!email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!password) {
      newErrors.password = 'Password is required';
    } else if (password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    setErrors(newErrors);
    return newErrors;
  };

  const handleLogin = async () => {
    const validationErrors = validateForm();
    const errorMessages = Object.values(validationErrors).filter(Boolean);
    if (errorMessages.length > 0) {
      showToast(errorMessages[0] || 'Please fill in all required fields', 'error');
      return;
    }

    setLoading(true);
    try {
      await login({ email: email.trim(), password });
      setLoading(false);
      showToast('Login successful!', 'success');
      router.replace('/(tabs)');
    } catch (err) {
      setLoading(false);
      const errorMessage = err instanceof Error ? err.message : 'Invalid credentials';
      if (errorMessage.toLowerCase().includes('email') || errorMessage.toLowerCase().includes('user not found')) {
        setErrors({ email: 'No account found with this email' });
        showToast('No account found with this email', 'error');
      } else if (errorMessage.toLowerCase().includes('password') || errorMessage.toLowerCase().includes('invalid')) {
        setErrors({ password: 'Incorrect password' });
        showToast('Incorrect password', 'error');
      } else {
        showToast(errorMessage, 'error');
      }
    }
  };

  const handleFallbackGoogleLogin = () => {
    if (!GOOGLE_CLIENT_ID) {
      showToast('Google Sign-In is not configured', 'info');
      return;
    }
    showToast('Loading Google Sign-In...', 'info');
  };

  return (
    <ScreenWrapper scrollable padded backgroundColor={COLORS.white}>
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.logo}>JENDO</Text>
          <Text style={styles.title}>Welcome Back</Text>
          <Text style={styles.subtitle}>Login to your account</Text>
        </View>

        <View style={styles.form}>
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Email</Text>
            <View style={[styles.inputWrapper, errors.email && localStyles.inputError]}>
              <Ionicons name="mail-outline" size={20} color={errors.email ? '#FF5252' : COLORS.textSecondary} style={styles.inputIcon} />
              <Input
                value={email}
                onChangeText={(text) => {
                  setEmail(text);
                  if (errors.email) setErrors({...errors, email: undefined});
                }}
                placeholder="Enter your email"
                keyboardType="email-address"
                autoCapitalize="none"
                style={styles.input}
              />
            </View>
            {errors.email && <Text style={localStyles.errorText}>{errors.email}</Text>}
          </View>

          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Password</Text>
            <View style={[styles.inputWrapper, errors.password && localStyles.inputError]}>
              <Ionicons name="lock-closed-outline" size={20} color={errors.password ? '#FF5252' : COLORS.textSecondary} style={styles.inputIcon} />
              <Input
                value={password}
                onChangeText={(text) => {
                  setPassword(text);
                  if (errors.password) setErrors({...errors, password: undefined});
                }}
                placeholder="Enter your password"
                secureTextEntry={!showPassword}
                style={styles.input}
              />
              <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={styles.eyeIcon}>
                <Ionicons name={showPassword ? 'eye-off-outline' : 'eye-outline'} size={20} color={COLORS.textSecondary} />
              </TouchableOpacity>
            </View>
            {errors.password && <Text style={localStyles.errorText}>{errors.password}</Text>}
          </View>

          <TouchableOpacity onPress={() => router.push('/auth/forgot-password')} style={styles.forgotPassword}>
            <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
          </TouchableOpacity>

          <Button title="Login" onPress={handleLogin} loading={loading} style={styles.primaryButton} />

          <View style={styles.divider}>
            <View style={styles.dividerLine} />
            <Text style={styles.dividerText}>Or continue with</Text>
            <View style={styles.dividerLine} />
          </View>

          {Platform.OS === 'web' && GOOGLE_CLIENT_ID ? (
            <View style={localStyles.googleButtonContainer}>
              {googleLoading && (
                <View style={localStyles.googleLoadingOverlay}>
                  <Text style={localStyles.loadingText}>Signing in...</Text>
                </View>
              )}
              <View 
                ref={googleButtonRef} 
                style={localStyles.googleButton}
                {...({ 'data-google-button': 'true' } as any)}
              />
            </View>
          ) : (
            <TouchableOpacity 
              style={styles.socialButton} 
              onPress={handleNativeGoogleLogin}
              disabled={googleLoading}
            >
              <Ionicons name="logo-google" size={20} color="#DB4437" />
              <Text style={styles.socialButtonText}>
                {googleLoading ? 'Signing in...' : 'Continue with Google'}
              </Text>
            </TouchableOpacity>
          )}
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Don't have an account? </Text>
          <TouchableOpacity onPress={() => router.push('/auth/signup')}>
            <Text style={styles.footerLink}>Sign Up</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScreenWrapper>
  );
};

const localStyles = StyleSheet.create({
  inputError: {
    borderColor: '#FF5252',
    borderWidth: 1,
  },
  errorText: {
    color: '#FF5252',
    fontSize: TYPOGRAPHY.fontSize.sm,
    marginTop: SPACING.xs,
  },
  googleButtonContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 50,
    position: 'relative' as const,
  },
  googleButton: {
    alignSelf: 'center',
  },
  googleLoadingOverlay: {
    position: 'absolute' as const,
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
  loadingText: {
    color: COLORS.primary,
    fontSize: TYPOGRAPHY.fontSize.sm,
  },
});
