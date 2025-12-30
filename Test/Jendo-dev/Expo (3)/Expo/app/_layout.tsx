import React, { useEffect, useCallback } from 'react';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider as PaperProvider } from 'react-native-paper';
import { useFonts } from 'expo-font';
import * as SplashScreen from 'expo-splash-screen';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { 
  Ionicons, 
  MaterialIcons, 
  MaterialCommunityIcons,
  FontAwesome, 
  FontAwesome5,
  Feather,
  Entypo, 
  AntDesign 
} from '@expo/vector-icons';
import 'react-native-reanimated';

import { ThemeProvider } from '../src/providers/ThemeProvider';
import { QueryProvider } from '../src/providers/QueryProvider';
import { AuthProvider } from '../src/providers/AuthProvider';
import { ToastProvider } from '../src/providers/ToastProvider';
import { database } from '../src/infrastructure/database';
import { initPushForUser } from '../src/services/pushNotifications';
import { useAuth } from '../src/providers/AuthProvider';

SplashScreen.preventAutoHideAsync();

const ONBOARDING_KEY = 'hasSeenOnboarding';

// ✅ NEW: Inner component to use auth context
function RootLayoutContent() {
  const { user, isLoading } = useAuth();
  const [fontsLoaded] = useFonts({
    ...Ionicons.font,
    ...MaterialIcons.font,
    ...MaterialCommunityIcons.font,
    ...FontAwesome.font,
    ...FontAwesome5.font,
    ...Feather.font,
    ...Entypo.font,
    ...AntDesign.font,
  });

  // ✅ Initialize push notifications when user is authenticated
  useEffect(() => {
    if (user?.id) {
      console.log('👤 User logged in, initializing push notifications');
      AsyncStorage.setItem('userId', String(user.id));
      initPushForUser(user.id);
    }
  }, [user?.id]);

  // ✅ Setup database and notification handler
  useEffect(() => {
    const setupApp = async () => {
      try {
        await database.initialize();
        
        // Set notification handler
        Notifications.setNotificationHandler({
          handleNotification: async () => ({
            shouldShowAlert: true,
            shouldPlaySound: true,
            shouldSetBadge: true,
            shouldShowBanner: true,
            shouldShowList: true,
          }),
        });

        console.log('✅ App initialization complete');
      } catch (error) {
        console.error('❌ Error initializing app:', error);
      }
    };

    setupApp();
  }, []);

  const onLayoutRootView = useCallback(async () => {
    if (fontsLoaded) {
      await SplashScreen.hideAsync();
    }
  }, [fontsLoaded]);

  useEffect(() => {
    if (fontsLoaded) {
      onLayoutRootView();
    }
  }, [fontsLoaded, onLayoutRootView]);

  if (!fontsLoaded || isLoading) {
    return null;
  }

  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="index" options={{ headerShown: false }} />
      <Stack.Screen name="onboarding" options={{ headerShown: false }} />
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="auth/login" options={{ headerShown: false }} />
      <Stack.Screen name="auth/signup" options={{ headerShown: false }} />
      <Stack.Screen name="auth/forgot-password" options={{ headerShown: false }} />
      <Stack.Screen name="auth/verify-otp" options={{ headerShown: false }} />
      <Stack.Screen name="notifications" options={{ headerShown: false }} />
      <Stack.Screen name="jendo-reports/[id]" options={{ headerShown: false }} />
      <Stack.Screen name="my-reports/[categoryId]" options={{ headerShown: false }} />
      <Stack.Screen name="my-reports/[categoryId]/[sectionId]" options={{ headerShown: false }} />
      <Stack.Screen name="my-reports/[categoryId]/[sectionId]/[itemId]" options={{ headerShown: false }} />
      <Stack.Screen name="my-reports/[categoryId]/[sectionId]/[itemId]/add" options={{ headerShown: false, presentation: 'modal' }} />
      <Stack.Screen name="my-reports/[categoryId]/[sectionId]/[itemId]/edit/[valueId]" options={{ headerShown: false }} />
      <Stack.Screen name="my-reports/add" options={{ headerShown: false, presentation: 'modal' }} />
      <Stack.Screen name="wellness/chatbot" options={{ headerShown: false }} />
      <Stack.Screen name="wellness/learning" options={{ headerShown: false }} />
      <Stack.Screen name="wellness/diet" options={{ headerShown: false }} />
      <Stack.Screen name="wellness/exercise" options={{ headerShown: false }} />
      <Stack.Screen name="wellness/sleep" options={{ headerShown: false }} />
      <Stack.Screen name="wellness/stress" options={{ headerShown: false }} />
      <Stack.Screen name="doctors/[id]/index" options={{ headerShown: false }} />
      <Stack.Screen name="doctors/[id]/book" options={{ headerShown: false, presentation: 'modal' }} />
      <Stack.Screen name="doctors/[id]/confirm" options={{ headerShown: false }} />
      <Stack.Screen name="doctors/[id]/payment" options={{ headerShown: false }} />
      <Stack.Screen name="doctors/[id]/confirmation" options={{ headerShown: false }} />
      <Stack.Screen name="appointments" options={{ headerShown: false }} />
      <Stack.Screen name="profile/personal" options={{ headerShown: false }} />
      <Stack.Screen name="profile/health" options={{ headerShown: false }} />
      <Stack.Screen name="profile/password" options={{ headerShown: false }} />
      <Stack.Screen name="profile/notifications" options={{ headerShown: false }} />
      <Stack.Screen name="+not-found" />
    </Stack>
  );
}

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <PaperProvider>
        <QueryProvider>
          <ThemeProvider>
            <AuthProvider>
              <ToastProvider>
                <StatusBar style="auto" />
                <RootLayoutContent />
              </ToastProvider>
            </AuthProvider>
          </ThemeProvider>
        </QueryProvider>
      </PaperProvider>
    </SafeAreaProvider>
  );
}