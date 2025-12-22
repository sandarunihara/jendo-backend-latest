import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, TextInput, Image, ActivityIndicator, Modal, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenWrapper } from '../../../common/components/layout';
import { EmptyState } from '../../../common/components/ui';
import { COLORS } from '../../../config/theme.config';
import { jendoTestApi, JendoTest } from '../services/jendoTestApi';
import { jendoStyles as styles } from '../components';
import { useAuth } from '../../../providers/AuthProvider';

type RiskLevel = 'low' | 'moderate' | 'high';

const getRiskColor = (level: RiskLevel) => {
  switch (level) {
    case 'low': return '#22C55E';
    case 'moderate': return '#F59E0B';
    case 'high': return '#EF4444';
    default: return COLORS.textSecondary;
  }
};

const getRiskBgColor = (level: RiskLevel) => {
  switch (level) {
    case 'low': return '#DCFCE7';
    case 'moderate': return '#FEF3C7';
    case 'high': return '#FEE2E2';
    default: return COLORS.surfaceSecondary;
  }
};

const getRiskLabel = (level: RiskLevel) => {
  switch (level) {
    case 'low': return 'Low Risk';
    case 'moderate': return 'Moderate Risk';
    case 'high': return 'High Risk';
    default: return 'Unknown';
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

export const JendoReportsScreen: React.FC = () => {
  const router = useRouter();
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [tests, setTests] = useState<JendoTest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterModalVisible, setFilterModalVisible] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState<string>('all');

  useEffect(() => {
    loadTests();
  }, [user]);

  const loadTests = async () => {
    try {
      setLoading(true);
      setError(null);

      if (user?.id) {
        console.log('Fetching Jendo tests for user:', user.id);
        const response = await jendoTestApi.getTestsByUserId(Number(user.id));
        console.log('Received tests:', response);
        setTests(response);
      } else {
        setTests([]);
      }
    } catch (err: any) {
      console.error('Error loading Jendo tests:', err);
      setError(err.message || 'Failed to load tests');
    } finally {
      setLoading(false);
    }
  };

  const filteredTests = tests.filter(test => {
    let matchesSearch = true;
    let matchesFilter = true;
    
    if (searchQuery) {
      const dateStr = formatDate(test.testDate);
      matchesSearch = dateStr.toLowerCase().includes(searchQuery.toLowerCase()) || 
             test.score.toString().includes(searchQuery);
    }
    
    if (selectedFilter !== 'all') {
      matchesFilter = test.riskLevel === selectedFilter;
    }
    
    return matchesSearch && matchesFilter;
  });

  const renderTestItem = ({ item }: { item: JendoTest }) => (
    <TouchableOpacity
      onPress={() => router.push(`/jendo-reports/${item.id}`)}
      activeOpacity={0.7}
      style={{
        backgroundColor: '#FFFFFF',
        borderRadius: 16,
        padding: 16,
        marginBottom: 12,
        borderWidth: 1,
        borderColor: '#E5E7EB',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 2,
      }}
    >
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <View style={{ flex: 1 }}>
          <Text style={{ fontSize: 14, color: '#6B7280', marginBottom: 4 }}>
            {formatDate(item.testDate)}
          </Text>
          <Text style={{ fontSize: 16, fontWeight: '600', color: '#1F2937', marginBottom: 8 }}>
            Jendo Vascular Health Report
          </Text>
        </View>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
          <Text style={{ fontSize: 28, fontWeight: '700', color: '#1F2937' }}>
            {item.score}
          </Text>
          <View style={{
            paddingHorizontal: 10,
            paddingVertical: 4,
            borderRadius: 12,
            backgroundColor: getRiskBgColor(item.riskLevel),
          }}>
            <Text style={{
              fontSize: 12,
              fontWeight: '600',
              color: getRiskColor(item.riskLevel),
            }}>
              {getRiskLabel(item.riskLevel)}
            </Text>
          </View>
        </View>
      </View>
      <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'flex-end', marginTop: 4 }}>
        <Ionicons name="chevron-forward" size={20} color="#9CA3AF" />
      </View>
    </TouchableOpacity>
  );

  const FilterModal = () => (
    <Modal
      animationType="slide"
      transparent={true}
      visible={filterModalVisible}
      onRequestClose={() => setFilterModalVisible(false)}
    >
      <Pressable 
        style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' }}
        onPress={() => setFilterModalVisible(false)}
      >
        <View style={{ backgroundColor: '#FFFFFF', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 20 }}>
          <Text style={{ fontSize: 18, fontWeight: '600', marginBottom: 16, textAlign: 'center' }}>Filter Reports</Text>
          
          {['all', 'low', 'moderate', 'high'].map((filter) => (
            <TouchableOpacity
              key={filter}
              onPress={() => {
                setSelectedFilter(filter);
                setFilterModalVisible(false);
              }}
              style={{
                flexDirection: 'row',
                alignItems: 'center',
                paddingVertical: 14,
                borderBottomWidth: filter !== 'high' ? 1 : 0,
                borderBottomColor: '#E5E7EB',
              }}
            >
              <View style={{
                width: 24,
                height: 24,
                borderRadius: 12,
                borderWidth: 2,
                borderColor: selectedFilter === filter ? COLORS.primary : '#D1D5DB',
                backgroundColor: selectedFilter === filter ? COLORS.primary : 'transparent',
                marginRight: 12,
                justifyContent: 'center',
                alignItems: 'center',
              }}>
                {selectedFilter === filter && (
                  <Ionicons name="checkmark" size={16} color="#FFFFFF" />
                )}
              </View>
              <Text style={{ fontSize: 16, color: '#1F2937' }}>
                {filter === 'all' ? 'All Reports' : getRiskLabel(filter as RiskLevel)}
              </Text>
            </TouchableOpacity>
          ))}
          
          <TouchableOpacity
            onPress={() => setFilterModalVisible(false)}
            style={{
              backgroundColor: COLORS.primary,
              borderRadius: 12,
              paddingVertical: 14,
              marginTop: 16,
              alignItems: 'center',
            }}
          >
            <Text style={{ color: '#FFFFFF', fontSize: 16, fontWeight: '600' }}>Apply Filter</Text>
          </TouchableOpacity>
        </View>
      </Pressable>
    </Modal>
  );

  return (
    <ScreenWrapper safeArea backgroundColor="#FFFFFF">
      <View style={{
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        paddingVertical: 12,
      }}>
        <View style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: '#F3E8FF', alignItems: 'center', justifyContent: 'center' }}>
          <Ionicons name="finger-print" size={24} color={COLORS.primary} />
        </View>
        <Text style={{ fontSize: 20, fontWeight: '700', color: COLORS.primary }}>Jendo Reports</Text>
        <TouchableOpacity 
          onPress={() => router.push('/notifications')}
          style={{ position: 'relative' }}
        >
          <Ionicons name="notifications" size={24} color={COLORS.primary} />
        </TouchableOpacity>
      </View>
      
      <View style={{ paddingHorizontal: 16, marginBottom: 12 }}>
        <View style={{
          flexDirection: 'row',
          alignItems: 'center',
          backgroundColor: '#F3F4F6',
          borderRadius: 12,
          paddingHorizontal: 12,
          paddingVertical: 10,
        }}>
          <Ionicons name="search-outline" size={20} color="#9CA3AF" />
          <TextInput
            style={{ flex: 1, marginLeft: 8, fontSize: 16, color: '#1F2937' }}
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholder="Search reports..."
            placeholderTextColor="#9CA3AF"
          />
          <TouchableOpacity onPress={() => setFilterModalVisible(true)}>
            <MaterialCommunityIcons name="filter-variant" size={24} color={COLORS.primary} />
          </TouchableOpacity>
        </View>
      </View>

      {loading ? (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={{ marginTop: 12, color: '#6B7280' }}>Loading your Jendo tests...</Text>
        </View>
      ) : error ? (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', paddingHorizontal: 20 }}>
          <Ionicons name="alert-circle-outline" size={64} color="#EF4444" />
          <Text style={{ fontSize: 18, fontWeight: '600', color: '#1F2937', marginTop: 16 }}>Error Loading Tests</Text>
          <Text style={{ fontSize: 14, color: '#6B7280', marginTop: 8, textAlign: 'center' }}>{error}</Text>
          <TouchableOpacity 
            style={{ backgroundColor: COLORS.primary, borderRadius: 12, paddingHorizontal: 24, paddingVertical: 12, marginTop: 16 }}
            onPress={loadTests}
          >
            <Text style={{ color: '#FFFFFF', fontWeight: '600' }}>Retry</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={filteredTests}
          renderItem={renderTestItem}
          keyExtractor={item => item.id}
          contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 20 }}
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <EmptyState
              icon="document-text-outline"
              title="No Reports Found"
              description={tests.length === 0 ? "You haven't taken any Jendo tests yet." : "No test reports match your search criteria."}
            />
          }
        />
      )}
      
      <FilterModal />
    </ScreenWrapper>
  );
};
