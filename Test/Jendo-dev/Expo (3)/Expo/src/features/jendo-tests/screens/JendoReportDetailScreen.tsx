import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, Dimensions, Linking, Platform, Alert } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenWrapper } from '../../../common/components/layout';
import { COLORS } from '../../../config/theme.config';
import { jendoReportApi, JendoReport } from '../services/jendoReportApi';
import { useAuth } from '../../../providers/AuthProvider';

const screenWidth = Dimensions.get('window').width;

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { 
    timeZone: 'Asia/Colombo',
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1048576).toFixed(1) + ' MB';
};

export const JendoReportDetailScreen: React.FC = () => {
  const { id } = useLocalSearchParams();
  const router = useRouter();
  const { user } = useAuth();
  const [report, setReport] = useState<JendoReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    loadReportDetails();
  }, [id]);

  const loadReportDetails = async () => {
    try {
      setLoading(true);
      const reportData = await jendoReportApi.getReportById(id as string);
      if (reportData) {
        setReport(reportData);
      } else {
        Alert.alert('Error', 'Report not found');
        router.back();
      }
    } catch (error: any) {
      console.error('Error loading report details:', error);
      Alert.alert('Error', 'Failed to load report details');
      router.back();
    } finally {
      setLoading(false);
    }
  };

  const handleViewPDF = async () => {
    if (!report) return;
    
    try {
      const downloadUrl = jendoReportApi.getDownloadUrl(report.id);
      
      // Open PDF for viewing (opens in browser on web, in PDF viewer on mobile)
      if (Platform.OS === 'web') {
        window.open(downloadUrl, '_blank');
      } else {
        const canOpen = await Linking.canOpenURL(downloadUrl);
        if (canOpen) {
          await Linking.openURL(downloadUrl);
        } else {
          Alert.alert('Error', 'Cannot open PDF viewer. Please ensure you have a PDF reader installed.');
        }
      }
    } catch (error) {
      console.error('View PDF error:', error);
      Alert.alert('Error', 'Failed to open PDF. Please try again.');
    }
  };

  const handleDownload = async () => {
    if (!report) return;
    
    setDownloading(true);
    try {
      const downloadUrl = jendoReportApi.getDownloadUrl(report.id);
      
      if (Platform.OS === 'web') {
        // Create a temporary anchor element to trigger download
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = report.originalFileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        Alert.alert('Success', 'Report download started.');
      } else {
        // On mobile, open URL which will prompt download/save
        await Linking.openURL(downloadUrl);
        Alert.alert('Success', 'Report download started. Check your downloads folder.');
      }
    } catch (error) {
      console.error('Download error:', error);
      Alert.alert('Error', 'Failed to download report. Please try again.');
    } finally {
      setDownloading(false);
    }
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
          <Text style={{ marginTop: 12, color: '#6B7280' }}>Loading report details...</Text>
        </View>
      </ScreenWrapper>
    );
  }

  if (!report) {
    return null;
  }

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
          Report Details
        </Text>
        <TouchableOpacity onPress={() => router.push('/notifications')}>
          <Ionicons name="notifications" size={24} color={COLORS.primary} />
        </TouchableOpacity>
      </View>

      <ScrollView 
        contentContainerStyle={{ padding: 16 }}
        showsVerticalScrollIndicator={false}
      >
        {/* PDF Preview Card */}
        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 16, 
          padding: 20, 
          marginBottom: 20,
          backgroundColor: '#FAFAFA',
          alignItems: 'center',
        }}>
          <MaterialCommunityIcons name="file-pdf-box" size={80} color="#EF4444" />
          <Text style={{ fontSize: 18, fontWeight: '600', color: '#1F2937', marginTop: 16, textAlign: 'center' }}>
            Jendo Vascular Health Report
          </Text>
          <Text style={{ fontSize: 14, color: '#6B7280', marginTop: 8 }}>
            {formatFileSize(report.fileSize)}
          </Text>
        </View>

        {/* Report Information */}
        <View style={{ 
          borderWidth: 1, 
          borderColor: '#E5E7EB', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
          backgroundColor: '#FFFFFF',
        }}>
          <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937', marginBottom: 16 }}>
            Report Information
          </Text>
          
          <View style={{ marginBottom: 12 }}>
            <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Report ID:</Text>
            <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>#{report.id}</Text>
          </View>

          <View style={{ marginBottom: 12 }}>
            <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Uploaded On:</Text>
            <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>
              {formatDate(report.uploadedAt)}
            </Text>
          </View>

          <View style={{ marginBottom: 12 }}>
            <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>File Name:</Text>
            <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>
              {report.fileName}
            </Text>
          </View>

          <View style={{ marginBottom: 12 }}>
            <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>File Type:</Text>
            <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937' }}>
              {report.contentType}
            </Text>
          </View>

          {report.description && (
            <View>
              <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Description:</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#1F2937', lineHeight: 20 }}>
                {report.description}
              </Text>
            </View>
          )}
        </View>

        {/* Important Notice */}
        <View style={{ 
          backgroundColor: '#DBEAFE', 
          borderRadius: 12, 
          padding: 16, 
          marginBottom: 20,
        }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 8 }}>
            <Ionicons name="information-circle" size={20} color="#1E40AF" />
            <Text style={{ fontSize: 12, fontWeight: '600', color: '#1E40AF', marginLeft: 8 }}>
              Important Information
            </Text>
          </View>
          <Text style={{ fontSize: 11, color: '#1E40AF', lineHeight: 16 }}>
            This is an official Jendo health report. Please consult with a qualified healthcare professional 
            for proper interpretation and medical advice regarding the contents of this report.
          </Text>
        </View>

        {/* Action Buttons */}
        <TouchableOpacity
          onPress={handleViewPDF}
          style={{
            backgroundColor: COLORS.primary,
            borderRadius: 12,
            paddingVertical: 16,
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: 12,
          }}
        >
          <MaterialCommunityIcons name="eye" size={20} color="#FFFFFF" />
          <Text style={{ color: '#FFFFFF', fontSize: 16, fontWeight: '600', marginLeft: 8 }}>
            View PDF Report
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={handleDownload}
          disabled={downloading}
          style={{
            backgroundColor: '#10B981',
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
