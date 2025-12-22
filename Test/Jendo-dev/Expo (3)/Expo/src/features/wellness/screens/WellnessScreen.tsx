import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenWrapper } from '../../../common/components/layout';
import { COLORS } from '../../../config/theme.config';
import { wellnessStyles as styles } from '../components';
import { useLearningMaterials } from '../../learning/hooks/useLearningMaterials';
import { LearningMaterial } from '../../learning/types';

const getCategoryColor = (category: string): { bg: string; text: string } => {
  const colors: Record<string, { bg: string; text: string }> = {
    'Mental Health': { bg: '#FCE4EC', text: '#E91E63' },
    'Stress': { bg: '#FCE4EC', text: '#E91E63' },
    'Nutrition': { bg: '#E8F5E9', text: '#4CAF50' },
    'Exercise': { bg: '#FFF3E0', text: '#FF9800' },
    'Cardiology': { bg: '#E3F2FD', text: '#2196F3' },
  };
  return colors[category] || { bg: '#F5F5F5', text: '#757575' };
};

export const WellnessScreen: React.FC = () => {
  const router = useRouter();
  const { data: learningData, loading: learningLoading } = useLearningMaterials(0, 5);

  return (
    <ScreenWrapper safeArea backgroundColor={COLORS.white}>
      <View style={styles.header}>
        <View style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: '#F3E8FF', alignItems: 'center', justifyContent: 'center' }}>
          <Ionicons name="finger-print" size={24} color={COLORS.primary} />
        </View>
        <Text style={styles.headerTitle}>Wellness Recommendations</Text>
        <TouchableOpacity 
          style={styles.notificationButton}
          onPress={() => router.push('/notifications')}
        >
          <Ionicons name="notifications" size={24} color={COLORS.primary} />
        </TouchableOpacity>
      </View>

      <ScrollView 
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        <View style={styles.healthPlanBanner}>
          <Text style={styles.healthPlanTitle}>Your Personal Health Plan</Text>
          <Text style={styles.healthPlanSubtitle}>
            Customized recommendations based on{'\n'}your health profile
          </Text>
          <View style={styles.riskIndicators}>
            <View style={styles.riskIndicator}>
              <View style={[styles.riskDot, { backgroundColor: '#4CAF50' }]} />
              <Text style={styles.riskText}>Low{'\n'}Risk</Text>
            </View>
            <View style={styles.riskIndicator}>
              <View style={[styles.riskDot, { backgroundColor: '#FF9800' }]} />
              <Text style={styles.riskText}>Medium{'\n'}Risk</Text>
            </View>
            <View style={styles.riskIndicator}>
              <View style={[styles.riskDot, { backgroundColor: '#E53935' }]} />
              <Text style={styles.riskText}>High{'\n'}Risk</Text>
            </View>
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Diet Suggestions</Text>
          <TouchableOpacity 
            style={styles.tipCard}
            onPress={() => router.push('/wellness/diet')}
          >
            <View style={[styles.tipIconContainer, { backgroundColor: '#E8F5E9' }]}>
              <MaterialCommunityIcons name="apple" size={24} color="#4CAF50" />
            </View>
            <View style={styles.tipContent}>
              <Text style={styles.tipTitle}>Mediterranean Diet</Text>
              <Text style={styles.tipDescription}>Rich in healthy fats and antioxidants</Text>
              <View style={styles.tipBadgeRow}>
                <View style={[styles.tipBadge, { backgroundColor: '#E8F5E9' }]}>
                  <Text style={[styles.tipBadgeText, { color: '#4CAF50' }]}>Heart Healthy</Text>
                </View>
                <Ionicons name="arrow-forward" size={16} color={COLORS.primary} />
              </View>
            </View>
          </TouchableOpacity>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Exercise Tips</Text>
          <TouchableOpacity 
            style={styles.tipCard}
            onPress={() => router.push('/wellness/exercise')}
          >
            <View style={[styles.tipIconContainer, { backgroundColor: '#FFF3E0' }]}>
              <MaterialCommunityIcons name="dumbbell" size={24} color="#FF9800" />
            </View>
            <View style={styles.tipContent}>
              <Text style={styles.tipTitle}>30-Min Morning Workout</Text>
              <Text style={styles.tipDescription}>Start your day with energy</Text>
              <View style={styles.tipBadgeRow}>
                <View style={[styles.tipBadge, { backgroundColor: '#FFF3E0' }]}>
                  <Text style={[styles.tipBadgeText, { color: '#FF9800' }]}>Beginner</Text>
                </View>
                <Ionicons name="arrow-forward" size={16} color={COLORS.primary} />
              </View>
            </View>
          </TouchableOpacity>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Sleep Tips</Text>
          <TouchableOpacity 
            style={styles.tipCard}
            onPress={() => router.push('/wellness/sleep')}
          >
            <View style={[styles.tipIconContainer, { backgroundColor: '#E3F2FD' }]}>
              <Ionicons name="moon" size={24} color="#2196F3" />
            </View>
            <View style={styles.tipContent}>
              <Text style={styles.tipTitle}>Better Sleep Hygiene</Text>
              <Text style={styles.tipDescription}>Create the perfect sleep environment</Text>
              <View style={styles.tipBadgeRow}>
                <View style={[styles.tipBadge, { backgroundColor: '#E3F2FD' }]}>
                  <Text style={[styles.tipBadgeText, { color: '#2196F3' }]}>7-8 Hours</Text>
                </View>
                <Ionicons name="arrow-forward" size={16} color={COLORS.primary} />
              </View>
            </View>
          </TouchableOpacity>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Stress Management</Text>
          <TouchableOpacity 
            style={styles.tipCard}
            onPress={() => router.push('/wellness/stress')}
          >
            <View style={[styles.tipIconContainer, { backgroundColor: '#FCE4EC' }]}>
              <MaterialCommunityIcons name="heart-pulse" size={24} color="#E91E63" />
            </View>
            <View style={styles.tipContent}>
              <Text style={styles.tipTitle}>Mindfulness Meditation</Text>
              <Text style={styles.tipDescription}>5-minute daily breathing exercises</Text>
              <View style={styles.tipBadgeRow}>
                <View style={[styles.tipBadge, { backgroundColor: '#FCE4EC' }]}>
                  <Text style={[styles.tipBadgeText, { color: '#E91E63' }]}>Relaxation</Text>
                </View>
                <Ionicons name="arrow-forward" size={16} color={COLORS.primary} />
              </View>
            </View>
          </TouchableOpacity>
        </View>

        <View style={styles.learnSection}>
          <View style={styles.learnHeader}>
            <View style={[styles.learnIconContainer, { backgroundColor: '#F3E5F5' }]}>
              <MaterialCommunityIcons name="lightbulb-on" size={24} color={COLORS.primary} />
            </View>
            <View style={styles.learnTitleContainer}>
              <Text style={styles.learnTitle}>Learn & Improve</Text>
              <Text style={styles.learnSubtitle}>Watch videos and explore wellness guides</Text>
            </View>
          </View>
          
          <ScrollView 
            horizontal 
            showsHorizontalScrollIndicator={false}
            style={styles.categoryFilters}
          >
            <TouchableOpacity style={[styles.categoryChip, styles.categoryChipActive]}>
              <Text style={[styles.categoryChipText, styles.categoryChipTextActive]}>All</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.categoryChip}>
              <Text style={styles.categoryChipText}>Diet</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.categoryChip}>
              <Text style={styles.categoryChipText}>Exercise</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.categoryChip}>
              <Text style={styles.categoryChipText}>Stress</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.categoryChip}>
              <Text style={styles.categoryChipText}>Sleep</Text>
            </TouchableOpacity>
          </ScrollView>

          <ScrollView 
            horizontal 
            showsHorizontalScrollIndicator={false}
            style={styles.videosScroll}
          >
            {learningLoading ? (
              <View style={{ padding: 20 }}>
                <ActivityIndicator size="small" color={COLORS.primary} />
              </View>
            ) : learningData && learningData.content.length > 0 ? (
              learningData.content.map((material: LearningMaterial) => {
                const categoryColor = getCategoryColor(material.category);
                return (
                  <TouchableOpacity 
                    key={material.id}
                    style={styles.videoCard}
                    onPress={() => router.push('/wellness/learning')}
                  >
                    <View style={[styles.videoThumbnail, { backgroundColor: categoryColor.bg }]}>
                      <View style={styles.playButton}>
                        <Ionicons name="play" size={20} color={COLORS.white} />
                      </View>
                      <View style={styles.videoDuration}>
                        <Text style={styles.videoDurationText}>{material.duration}</Text>
                      </View>
                    </View>
                    <Text style={styles.videoTitle} numberOfLines={2}>
                      {material.title}
                    </Text>
                    <View style={[styles.videoCategory, { backgroundColor: categoryColor.bg }]}>
                      <Text style={[styles.videoCategoryText, { color: categoryColor.text }]}>
                        {material.category}
                      </Text>
                    </View>
                  </TouchableOpacity>
                );
              })
            ) : (
              <View style={{ padding: 20 }}>
                <Text style={{ color: COLORS.textSecondary }}>No materials available</Text>
              </View>
            )}
          </ScrollView>
        </View>

        <TouchableOpacity 
          style={styles.chatbotCard}
          onPress={() => router.push('/wellness/chatbot')}
        >
          <View style={styles.chatbotIconContainer}>
            <MaterialCommunityIcons name="robot" size={28} color={COLORS.primary} />
          </View>
          <View style={styles.chatbotContent}>
            <Text style={styles.chatbotTitle}>Health Assistant</Text>
            <Text style={styles.chatbotDescription}>
              Ask questions about your heart health and get personalized advice
            </Text>
          </View>
          <Ionicons name="chevron-forward" size={24} color={COLORS.textMuted} />
        </TouchableOpacity>

        <View style={styles.bottomPadding} />
      </ScrollView>
    </ScreenWrapper>
  );
};
