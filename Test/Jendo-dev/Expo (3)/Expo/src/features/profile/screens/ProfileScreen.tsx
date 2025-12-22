import React from 'react';
import { View, Text, ScrollView, Image, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ScreenWrapper } from '../../../common/components/layout';
import { COLORS } from '../../../config/theme.config';
import { profileStyles as styles } from '../components';
import { useAuth } from '../../../providers/AuthProvider';
import { useUserStore } from '../../../state/userSlice';

const calculateBMI = (weight?: number, height?: number): { value: string; category: string; color: string } => {
  if (!weight || !height || height === 0) {
    return { value: '--', category: 'Not calculated', color: '#9E9E9E' };
  }
  const heightInMeters = height / 100;
  const bmi = weight / (heightInMeters * heightInMeters);
  const bmiValue = bmi.toFixed(1);
  
  if (bmi < 18.5) {
    return { value: bmiValue, category: 'Underweight', color: '#FF9800' };
  } else if (bmi < 25) {
    return { value: bmiValue, category: 'Normal', color: '#4CAF50' };
  } else if (bmi < 30) {
    return { value: bmiValue, category: 'Overweight', color: '#FF9800' };
  } else {
    return { value: bmiValue, category: 'Obese', color: '#F44336' };
  }
};

export const ProfileScreen: React.FC = () => {
  const router = useRouter();
  const { logout } = useAuth();
  const { user } = useUserStore();

  const userName = user ? [user.firstName, user.lastName].filter(Boolean).join(' ') || 'User' : 'User';
  const userEmail = user?.email || '';
  const userAvatar = user?.avatar || user?.profileImage;
  const defaultAvatar = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(userName) + '&background=7B2D8E&color=fff&size=200';

  const bmiData = calculateBMI(user?.weight, user?.height);

  const handleLogout = async () => {
    try {
      await AsyncStorage.removeItem('jwtToken');
      await logout();
      router.replace('/auth/login');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  return (
    <ScreenWrapper safeArea padded={false} backgroundColor={COLORS.white}>
      <View style={styles.header}>
        <Image source={{ uri: userAvatar || defaultAvatar }} style={styles.avatar} />
        <Text style={styles.headerTitle}>Profile</Text>
        <TouchableOpacity 
          style={styles.notificationButton}
          onPress={() => router.push('/notifications')}
        >
          <Ionicons name="notifications" size={24} color={COLORS.primary} />
        </TouchableOpacity>
      </View>

      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.profileSection}>
          <View style={styles.avatarContainer}>
            <Image source={{ uri: userAvatar || defaultAvatar }} style={styles.profileAvatar} />
            <TouchableOpacity style={styles.cameraButton}>
              <Ionicons name="camera" size={14} color={COLORS.white} />
            </TouchableOpacity>
          </View>
          <Text style={styles.userName}>{userName}</Text>
          <Text style={styles.membershipType}>Member</Text>
          <Text style={styles.userEmail}>{userEmail}</Text>
        </View>

        <View style={styles.menuSection}>
          <View style={styles.sectionHeader}>
            <Ionicons name="person" size={18} color={COLORS.textPrimary} />
            <Text style={styles.sectionTitle}>Personal Details</Text>
          </View>
          <TouchableOpacity 
            style={styles.menuItem}
            onPress={() => router.push('/profile/personal')}
          >
            <View style={styles.menuItemLeft}>
              <MaterialCommunityIcons name="account-edit" size={20} color={COLORS.primary} />
              <Text style={styles.menuItemText}>Change Details</Text>
            </View>
            <Ionicons name="chevron-forward" size={20} color={COLORS.textMuted} />
          </TouchableOpacity>
        </View>

        <View style={styles.menuSection}>
          <View style={styles.sectionHeader}>
            <Ionicons name="calendar" size={18} color={COLORS.textPrimary} />
            <Text style={styles.sectionTitle}>Appointments</Text>
          </View>
          <TouchableOpacity 
            style={styles.menuItem}
            onPress={() => router.push('/appointments')}
          >
            <View style={styles.menuItemLeft}>
              <MaterialCommunityIcons name="calendar-check" size={20} color={COLORS.primary} />
              <Text style={styles.menuItemText}>My Appointments</Text>
            </View>
            <Ionicons name="chevron-forward" size={20} color={COLORS.textMuted} />
          </TouchableOpacity>
        </View>

        <View style={styles.menuSection}>
          <View style={styles.sectionHeader}>
            <Ionicons name="lock-closed" size={18} color={COLORS.textPrimary} />
            <Text style={styles.sectionTitle}>Security</Text>
          </View>
          <TouchableOpacity 
            style={styles.menuItem}
            onPress={() => router.push('/profile/password')}
          >
            <View style={styles.menuItemLeft}>
              <MaterialCommunityIcons name="key-variant" size={20} color={COLORS.primary} />
              <Text style={styles.menuItemText}>Change Password</Text>
            </View>
            <Ionicons name="chevron-forward" size={20} color={COLORS.textMuted} />
          </TouchableOpacity>
        </View>

        <View style={styles.menuSection}>
          <View style={styles.sectionHeader}>
            <MaterialCommunityIcons name="heart-pulse" size={18} color={COLORS.textPrimary} />
            <Text style={styles.sectionTitle}>Health Parameters</Text>
          </View>
          <View style={styles.healthParamsContainer}>
            <View style={styles.healthParamCard}>
              <View style={[styles.healthParamIcon, { backgroundColor: '#FCE4EC' }]}>
                <MaterialCommunityIcons name="human-male-height" size={20} color="#E91E63" />
              </View>
              <Text style={styles.healthParamLabel}>Height</Text>
              <Text style={styles.healthParamValue}>
                {user?.height ? `${user.height}` : '--'}
              </Text>
              <Text style={styles.healthParamMetric}>
                {user?.height ? 'cm' : 'Not set'}
              </Text>
            </View>
            <View style={styles.healthParamCard}>
              <View style={[styles.healthParamIcon, { backgroundColor: '#F3E5F5' }]}>
                <MaterialCommunityIcons name="scale-bathroom" size={20} color={COLORS.primary} />
              </View>
              <Text style={styles.healthParamLabel}>Weight</Text>
              <Text style={styles.healthParamValue}>
                {user?.weight ? `${user.weight}` : '--'}
              </Text>
              <Text style={styles.healthParamMetric}>
                {user?.weight ? 'kg' : 'Not set'}
              </Text>
            </View>
            <View style={styles.healthParamCard}>
              <View style={[styles.healthParamIcon, { backgroundColor: '#E8F5E9' }]}>
                <MaterialCommunityIcons name="chart-donut" size={20} color="#4CAF50" />
              </View>
              <Text style={styles.healthParamLabel}>BMI</Text>
              <Text style={styles.healthParamValue}>{bmiData.value}</Text>
              <Text style={[styles.healthParamStatus, { color: bmiData.color }]}>{bmiData.category}</Text>
            </View>
          </View>
        </View>

        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Ionicons name="log-out-outline" size={20} color={COLORS.primary} />
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>

        <View style={styles.bottomPadding} />
      </ScrollView>
    </ScreenWrapper>
  );
};
