import { API_ENDPOINTS, httpClient } from '../../../infrastructure/api';

export interface WellnessRecommendation {
  id: number;
  title: string;
  description: string;
  category: string;
  riskLevel: string;
  type: string;
  priority: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp?: string;
}

export const wellnessRecommendationApi = {
  getByRiskLevel: async (riskLevel: string): Promise<WellnessRecommendation[]> => {
    try {
      const response = await httpClient.get<ApiResponse<WellnessRecommendation[]>>(
        API_ENDPOINTS.WELLNESS.BY_RISK_LEVEL(riskLevel.toUpperCase())
      );
      return response.data || [];
    } catch (error) {
      console.error('Error fetching wellness recommendations:', error);
      return [];
    }
  },

  getForUser: async (userId: number): Promise<WellnessRecommendation[]> => {
    try {
      const response = await httpClient.get<ApiResponse<WellnessRecommendation[]>>(
        API_ENDPOINTS.WELLNESS.FOR_USER(userId)
      );
      return response.data || [];
    } catch (error) {
      console.error('Error fetching user wellness recommendations:', error);
      return [];
    }
  },
};
