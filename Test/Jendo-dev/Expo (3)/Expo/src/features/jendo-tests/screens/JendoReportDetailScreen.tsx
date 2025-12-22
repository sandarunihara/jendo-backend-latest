import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, Dimensions, Linking, Platform } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { LineChart } from 'react-native-gifted-charts';
import { ScreenWrapper } from '../../../common/components/layout';
import { COLORS } from '../../../config/theme.config';
import { jendoTestApi, JendoTest } from '../services/jendoTestApi';
import { useAuth } from '../../../providers/AuthProvider';

const screenWidth = Dimensions.get('window').width;

const calculateAge = (dateOfBirth: string | undefined): number | null => {
  if (!dateOfBirth) return null;
  try {
    const birthDate = new Date(dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  } catch {
    return null;
  }
};

const getRiskColor = (level: string) => {
  switch (level?.toLowerCase()) {
    case 'low': return '#22C55E';
    case 'moderate': return '#F59E0B';
    case 'high': return '#EF4444';
    default: return '#6B7280';
  }
};

const getRiskLabel = (level: string) => {
  switch (level?.toLowerCase()) {
    case 'low': return 'Low';
    case 'moderate': return 'Moderate';
    case 'high': return 'High';
    default: return 'Unknown';
  }
};

const SimpleLineChart: React.FC<{ 
  data: number[]; 
  color: string; 
  title: string;
  yLabels?: string[];
  xLabels?: string[];
}> = ({ data, color, title, yLabels, xLabels }) => {
  const chartWidth = Math.min(screenWidth - 80, 600);
  const isSmallScreen = screenWidth < 400;
  
  const chartData = data.map((value, index) => ({
    value: value,
    label: xLabels ? xLabels[Math.floor((index / data.length) * xLabels.length)] : String(index),
  }));

  return (
    <View style={{ marginBottom: 16, alignItems: 'center' }}>
      <LineChart
        data={chartData}
        width={chartWidth}
        height={120}
        spacing={chartWidth / data.length}
        initialSpacing={10}
        endSpacing={10}
        adjustToWidth={true}
        thickness={2.5}
        color={color}
        startFillColor={`${color}15`}
        endFillColor={`${color}05`}
        startOpacity={0.2}
        endOpacity={0.05}
        areaChart
        curved
        hideDataPoints={true}
        hideRules
        hideYAxisText={false}
        yAxisColor="#E5E7EB"
        yAxisThickness={1}
        xAxisColor="#E5E7EB"
        xAxisThickness={1}
        yAxisTextStyle={{
          color: '#9CA3AF',
          fontSize: isSmallScreen ? 8 : 10,
          fontWeight: '500',
        }}
        noOfSections={4}
        yAxisLabelWidth={30}
        animateOnDataChange
        animationDuration={600}
      />
      <View style={{ 
        flexDirection: 'row', 
        alignItems: 'center', 
        justifyContent: 'center', 
        marginTop: 12 
      }}>
        <View style={{ 
          width: 12, 
          height: 3, 
          backgroundColor: color, 
          marginRight: 6,
          borderRadius: 1.5 
        }} />
        <Text style={{ 
          fontSize: isSmallScreen ? 10 : 12, 
          color: '#6B7280',
          fontWeight: '500'
        }}>
          {title}
        </Text>
      </View>
    </View>
  );
};

export const JendoReportDetailScreen: React.FC = () => {
  const { id } = useLocalSearchParams();
  const router = useRouter();
  const { user } = useAuth();
  const [test, setTest] = useState<JendoTest | null>(null);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  const userAge = calculateAge(user?.dateOfBirth);

  useEffect(() => {
    loadTestDetails();
  }, [id]);

  const loadTestDetails = async () => {
    try {
      setLoading(true);
      const testData = await jendoTestApi.getTestById(id as string);
      if (testData) {
        setTest(testData);
      } else {
        router.back();
      }
    } catch (error: any) {
      console.error('Error loading test details:', error);
      router.back();
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      timeZone: 'Asia/Colombo',
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  };

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const baseUrl = process.env.EXPO_PUBLIC_API_URL || '/api';
      const pdfUrl = `${baseUrl}/jendo-tests/${id}/report.pdf`;
      if (Platform.OS === 'web') {
        window.open(pdfUrl, '_blank');
      } else {
        await Linking.openURL(pdfUrl);
      }
    } catch (error) {
      console.error('Download error:', error);
    } finally {
      setDownloading(false);
    }
  };

  const generatePulseData = () => {
    return Array.from({ length: 50 }, () => Math.random() * 40 + 30);
  };

  const generateTempData = () => {
    return Array.from({ length: 30 }, (_, i) => {
      const x = i / 30;
      return 0.6 - 0.1 * Math.sin(x * Math.PI) + Math.random() * 0.05;
    });
  };

  if (loading) {
    return (
      <ScreenWrapper safeArea backgroundColor="#FFFFFF">
        <View style={{
          flexDirection: 'row',
          alignItems: 'center',
          paddingHorizontal: 16,
          paddingVertical: 12,
        }}>
          <TouchableOpacity onPress={() => router.back()}>
            <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={{ flex: 1, fontSize: 20, fontWeight: '700', color: '#1F2937', textAlign: 'center' }}>
            Loading...
          </Text>
          <View style={{ width: 24 }} />
        </View>
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={{ marginTop: 12, color: '#6B7280' }}>Loading test details...</Text>
        </View>
      </ScreenWrapper>
    );
  }

  if (!test) {
    return null;
  }

  const pulseData = generatePulseData();
  const tempData = generateTempData();
  // Use real backend data for vascularRisk, fallback to calculated value from score
  const vascularRisk = test.vascularRisk ?? (test.score ? (100 - test.score) : 0);
  // Use real backend data for spo2, fallback to default
  const spo2 = test.spo2 ?? 98.7;

  return (
    <ScreenWrapper safeArea backgroundColor="#FFFFFF">
      <View style={{
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        paddingVertical: 12,
      }}>
        <TouchableOpacity onPress={() => router.back()}>
          <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
        </TouchableOpacity>
        <Text style={{ fontSize: 20, fontWeight: '700', color: '#1F2937' }}>
          {formatDate(test.testDate)}
        </Text>
        <TouchableOpacity onPress={() => router.push('/notifications')}>
          <Ionicons name="notifications" size={24} color={COLORS.primary} />
        </TouchableOpacity>
      </View>

      <ScrollView 
        contentContainerStyle={{ padding: 16 }}
        showsVerticalScrollIndicator={false}
      >
        <Text style={{ fontSize: 22, fontWeight: '700', color: '#1F2937', textAlign: 'center', marginBottom: 20 }}>
          Vascular Health Report
        </Text>

        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
          backgroundColor: '#FAFAFA',
        }}>
          <View style={{ flexDirection: 'row', marginBottom: 12 }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Report ID:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>eabcd</Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Operator:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>A. Tom</Text>
            </View>
          </View>
          <View style={{ flexDirection: 'row', marginBottom: 12 }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Time:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>{test.testTime || '02:49'}</Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Institute:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>Jendo AI Health</Text>
            </View>
          </View>
          <View style={{ flexDirection: 'row' }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Patient:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>{test.userName || 'User'}</Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Age:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>{userAge ?? '--'}</Text>
            </View>
          </View>
        </View>

        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
          backgroundColor: '#FFFFFF',
        }}>
          <View style={{ flexDirection: 'row', marginBottom: 12 }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Vascular Risk:</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: getRiskColor(test.riskLevel) }}>
                {vascularRisk.toFixed(1)}%
              </Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>SpO2:</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937' }}>{spo2}%</Text>
            </View>
          </View>
          <View style={{ flexDirection: 'row', marginBottom: 12 }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Systolic BP:</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937' }}>
                {test.bloodPressureSystolic || 147} mmHg
              </Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Diastolic BP:</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937' }}>
                {test.bloodPressureDiastolic || 79} mmHg
              </Text>
            </View>
          </View>
          <View style={{ flexDirection: 'row' }}>
            <View style={{ flex: 1 }}>
              <Text style={{ fontSize: 12, color: '#6B7280' }}>Heart Rate:</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937' }}>
                {test.heartRate || 76} bpm
              </Text>
            </View>
          </View>
        </View>

        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
        }}>
          <SimpleLineChart 
            data={pulseData} 
            color="#EF4444" 
            title="Identity Pulse"
            yLabels={['100', '75', '50', '25', '0']}
            xLabels={['0', '100', '200', '300', '400']}
          />
        </View>

        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
        }}>
          <SimpleLineChart 
            data={tempData.map(v => v * 100)} 
            color="#EF4444" 
            title="Temperature Signal"
            yLabels={['0.6', '0.5', '0.4']}
            xLabels={['0', '500', '1,000', '1,500', '2,000', '2,500']}
          />
        </View>

        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          overflow: 'hidden',
          marginBottom: 20,
        }}>
          <View style={{ flexDirection: 'row', backgroundColor: '#F3F4F6' }}>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, fontWeight: '600', color: '#374151', textAlign: 'center' }}>
              Risk Level
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, fontWeight: '600', color: '#22C55E', textAlign: 'center' }}>
              Low
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, fontWeight: '600', color: '#F59E0B', textAlign: 'center' }}>
              Moderate
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, fontWeight: '600', color: '#EF4444', textAlign: 'center' }}>
              High
            </Text>
          </View>
          <View style={{ flexDirection: 'row', borderTopWidth: 1, borderColor: '#E5E7EB' }}>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, color: '#374151', textAlign: 'center' }}>
              Risk Score
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, color: '#374151', textAlign: 'center' }}>
              0-50
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, color: '#374151', textAlign: 'center' }}>
              51-75
            </Text>
            <Text style={{ flex: 1, padding: 10, fontSize: 12, color: '#374151', textAlign: 'center' }}>
              76-100
            </Text>
          </View>
        </View>

        <View style={{ 
          backgroundColor: '#FEF3C7', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
        }}>
          <Text style={{ fontSize: 12, fontWeight: '600', color: '#92400E', marginBottom: 8 }}>
            Disclaimer:
          </Text>
          <Text style={{ fontSize: 11, color: '#92400E', lineHeight: 16 }}>
            This report is generated by an artificial intelligence (AI) system based on the information provided. 
            While the AI has been trained to detect cardiovascular conditions, it may not identify issues accurately, 
            and its results should not be considered definitive. The AI system does not replace professional medical 
            advice, diagnosis, or treatment. If you experience any symptoms or have concerns about your vascular health, 
            please seek consultation with a qualified healthcare professional.
          </Text>
        </View>

        <TouchableOpacity
          onPress={handleDownload}
          disabled={downloading}
          style={{
            backgroundColor: COLORS.primary,
            borderRadius: 12,
            paddingVertical: 16,
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: 30,
          }}
        >
          {downloading ? (
            <ActivityIndicator size="small" color="#FFFFFF" />
          ) : (
            <>
              <MaterialCommunityIcons name="download" size={20} color="#FFFFFF" />
              <Text style={{ color: '#FFFFFF', fontSize: 16, fontWeight: '600', marginLeft: 8 }}>
                Download Report
              </Text>
            </>
          )}
        </TouchableOpacity>
      </ScrollView>
    </ScreenWrapper>
  );
};
