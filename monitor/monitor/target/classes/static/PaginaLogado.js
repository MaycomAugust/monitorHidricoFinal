import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  Modal,
  TextInput,
  ActivityIndicator,
} from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';
import axios from 'axios';

const API_BASE_URL = 'http://10.0.2.2:8081/api';

export default function PaginaLogado({ navigation, route }) {
  const [user, setUser] = useState(null);
  const [sensors, setSensors] = useState([]);
  const [activeSensorsCount, setActiveSensorsCount] = useState(0);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState('');
  const [loading, setLoading] = useState(true);

  // Estados para o modal de edição
  const [name, setName] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  useEffect(() => {
    loadUserData();
    loadSensors();
    
    // Polling para atualizações a cada 5 segundos
    const interval = setInterval(loadSensors, 5000);
    
    return () => clearInterval(interval);
  }, []);

  const loadUserData = async () => {
  try {
    const token = await getToken();
    
    if (!token) {
      Alert.alert('Erro', 'Sessão expirada. Faça login novamente.');
      navigation.navigate('Login');
      return;
    }

    console.log('Fazendo requisição para:', `${API_BASE_URL}/users/me`);
    
    const response = await axios.get(`${API_BASE_URL}/users/me`, {
      headers: { 
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      timeout: 10000 // 10 segundos timeout
    });
    
    console.log('Resposta da API:', response.status);
    setUser(response.data);
    
  } catch (error) {
    console.error('Erro detalhado ao carregar usuário:', error);
    
    if (error.response) {
      // O servidor respondeu com status de erro
      console.log('Status:', error.response.status);
      console.log('Headers:', error.response.headers);
      console.log('Data:', error.response.data);
      
      if (error.response.status === 403) {
        Alert.alert(
          'Acesso Negado', 
          'Sessão expirada ou token inválido. Faça login novamente.',
          [{ text: 'OK', onPress: () => navigation.navigate('Login') }]
        );
      } else if (error.response.status === 401) {
        Alert.alert(
          'Não Autorizado',
          'Faça login para acessar esta funcionalidade.',
          [{ text: 'OK', onPress: () => navigation.navigate('Login') }]
        );
      }
    } else if (error.request) {
      // A requisição foi feita mas não houve resposta
      console.log('Não houve resposta do servidor:', error.request);
      Alert.alert('Erro', 'Servidor não respondeu. Verifique a conexão.');
    } else {
      // Outro erro
      console.log('Erro na configuração:', error.message);
    }
    
    // Usar dados mock como fallback
    setUser({
      nome: 'Maria Silva',
      email: 'maria@email.com'
    });
  }
};

  const loadSensors = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/sensors`);
      const activeSensors = response.data.filter(sensor => sensor.ativo);
      setSensors(response.data);
      setActiveSensorsCount(activeSensors.length);
      setLoading(false);
    } catch (error) {
      console.error('Erro ao carregar sensores:', error);
      // Usar dados mock para teste
      const mockSensors = [
        {
          id: 1,
          nome: 'Sensor Rio Principal',
          localicao: 'Rio Principal',
          latitude: -23.5505,
          longitude: -46.6333,
          status: 'ALERTA',
          ativo: true
        },
        {
          id: 2,
          nome: 'Represa Leste',
          localicao: 'Represa Leste',
          latitude: -23.5605,
          longitude: -46.6433,
          status: 'NORMAL',
          ativo: true
        },
        {
          id: 3,
          nome: 'Poço Artesiano',
          localicao: 'Zona Norte',
          latitude: -23.5405,
          longitude: -46.6233,
          status: 'CRITICO',
          ativo: true
        },
        {
          id: 4,
          nome: 'Reservatório Sul',
          localicao: 'Zona Sul',
          latitude: -23.5705,
          longitude: -46.6533,
          status: 'NORMAL',
          ativo: true
        }
      ];
      setSensors(mockSensors);
      setActiveSensorsCount(mockSensors.filter(s => s.ativo).length);
      setLoading(false);
    }
  };

 const getToken = async () => {
  try {
    // Verifique se o token existe no localStorage (ou AsyncStorage)
    const token = route.params?.token || await AsyncStorage.getItem('userToken');
    
    if (!token) {
      console.log('Token não encontrado');
      navigation.navigate('Login');
      return null;
    }
    
    console.log('Token encontrado:', token.substring(0, 20) + '...');
    return token;
  } catch (error) {
    console.log('Erro ao buscar token:', error);
    navigation.navigate('Login');
    return null;
  }
};

  const getStatusColor = (status) => {
    switch(status) {
      case 'NORMAL': return '#7ed957';
      case 'ATENCAO': return '#ffd36b';
      case 'ALERTA': return '#ff8a7a';
      case 'CRITICO': return '#ff4757';
      default: return '#7d8c93';
    }
  };

  const getStatusIcon = (status) => {
    switch(status) {
      case 'NORMAL': return 'check-circle';
      case 'ATENCAO': return 'exclamation-circle';
      case 'ALERTA': return 'exclamation-triangle';
      case 'CRITICO': return 'times-circle';
      default: return 'question-circle';
    }
  };

  const openEditModal = (type) => {
    setModalType(type);
    setModalVisible(true);
    
    if (type === 'name' && user) {
      setName(user.nome);
    }
  };

  const closeModal = () => {
    setModalVisible(false);
    setModalType('');
    setName('');
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
  };

  const saveChanges = async () => {
    const token = await getToken();
    
    try {
      switch(modalType) {
        case 'name':
          await updateName(token);
          break;
        case 'password':
          await updatePassword(token);
          break;
        default:
          break;
      }
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível salvar as alterações');
    }
  };

  const updateName = async (token) => {
    if (!name.trim()) {
      Alert.alert('Erro', 'Nome é obrigatório!');
      return;
    }

    try {
      const response = await axios.put(
        `${API_BASE_URL}/users/me/name`,
        { nome: name },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setUser(response.data);
      Alert.alert('Sucesso', 'Nome atualizado com sucesso!');
      closeModal();
    } catch (error) {
      Alert.alert('Erro', error.response?.data?.message || 'Erro ao atualizar nome');
    }
  };

  const updatePassword = async (token) => {
    if (!currentPassword) {
      Alert.alert('Erro', 'Senha atual é obrigatória!');
      return;
    }

    if (!newPassword || newPassword.length < 6) {
      Alert.alert('Erro', 'Nova senha deve ter pelo menos 6 caracteres!');
      return;
    }

    if (newPassword !== confirmPassword) {
      Alert.alert('Erro', 'As senhas não coincidem!');
      return;
    }

    try {
      await axios.put(
        `${API_BASE_URL}/users/me/password`,
        {
          currentPassword,
          newPassword,
          confirmPassword
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      Alert.alert('Sucesso', 'Senha alterada com sucesso!');
      closeModal();
    } catch (error) {
      Alert.alert('Erro', error.response?.data?.message || 'Erro ao alterar senha');
    }
  };

  const deleteAccount = () => {
    Alert.alert(
      'Confirmar Exclusão',
      '⚠️ ATENÇÃO: Tem certeza que deseja excluir sua conta? TODOS os seus dados serão perdidos permanentemente e esta ação NÃO pode ser desfeita.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Excluir',
          style: 'destructive',
          onPress: () => confirmDeleteAccount()
        }
      ]
    );
  };

  const confirmDeleteAccount = async () => {
    const token = await getToken();
    
    try {
      const response = await axios.delete(
        `${API_BASE_URL}/users/me`,
        {
          headers: { Authorization: `Bearer ${token}` },
          data: { password: currentPassword }
        }
      );
      
      if (response.ok) {
        Alert.alert('Sucesso', 'Conta excluída com sucesso!');
        setTimeout(() => {
          navigation.navigate('Login');
        }, 2000);
      }
    } catch (error) {
      Alert.alert('Erro', error.response?.data?.message || 'Erro ao excluir conta');
    }
  };

  const logout = () => {
    Alert.alert(
      'Sair',
      'Deseja realmente sair?',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Sair',
          style: 'destructive',
          onPress: () => navigation.navigate('Login')
        }
      ]
    );
  };

  const renderSensorsGrid = () => (
    <View style={styles.heroCard}>
      <Text style={styles.sectionTitle}>Sensores Ativos</Text>
      
      <View style={styles.sensorsGrid}>
        {sensors
          .filter(sensor => sensor.ativo)
          .slice(0, 6) // Mostra apenas os 6 primeiros
          .map((sensor, index) => (
            <View key={sensor.id || index} style={styles.sensorCard}>
              <View style={[
                styles.sensorStatus, 
                { backgroundColor: getStatusColor(sensor.status) }
              ]}>
                <Icon 
                  name={getStatusIcon(sensor.status)} 
                  size={20} 
                  color="#fff" 
                />
              </View>
              <Text style={styles.sensorName} numberOfLines={1}>
                {sensor.nome}
              </Text>
              <Text style={styles.sensorLocation} numberOfLines={1}>
                {sensor.localicao || 'Local não informado'}
              </Text>
              <Text style={[
                styles.sensorStatusText,
                { color: getStatusColor(sensor.status) }
              ]}>
                {sensor.status}
              </Text>
            </View>
          ))}
      </View>

      {/* Legenda */}
      <View style={styles.mapLegend}>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, styles.greenDot]} />
          <Text style={styles.legendText}>Normal</Text>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, styles.yellowDot]} />
          <Text style={styles.legendText}>Alerta</Text>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, styles.redDot]} />
          <Text style={styles.legendText}>Crítico</Text>
        </View>
      </View>
    </View>
  );

  const renderEditModal = () => {
    return (
      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={closeModal}
      >
        <View style={styles.modalContainer}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>
              {modalType === 'name' ? 'Alterar Nome' : 
               modalType === 'password' ? 'Alterar Senha' : 'Editar Perfil'}
            </Text>

            {modalType === 'name' && (
              <TextInput
                style={styles.input}
                placeholder="Seu nome completo"
                value={name}
                onChangeText={setName}
              />
            )}

            {modalType === 'password' && (
              <>
                <TextInput
                  style={styles.input}
                  placeholder="Senha atual"
                  secureTextEntry
                  value={currentPassword}
                  onChangeText={setCurrentPassword}
                />
                <TextInput
                  style={styles.input}
                  placeholder="Nova senha (mínimo 6 caracteres)"
                  secureTextEntry
                  value={newPassword}
                  onChangeText={setNewPassword}
                />
                <TextInput
                  style={styles.input}
                  placeholder="Confirmar nova senha"
                  secureTextEntry
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                />
              </>
            )}

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.button, styles.ghostButton]}
                onPress={closeModal}
              >
                <Text style={styles.ghostButtonText}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.button, styles.primaryButton]}
                onPress={saveChanges}
              >
                <Text style={styles.primaryButtonText}>Salvar</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#00b4c6" />
        <Text style={styles.loadingText}>Carregando...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.brand}>
          <View style={styles.dropIcon}>
            <Icon name="tint" size={24} color="#fff" />
          </View>
          <View>
            <Text style={styles.brandTitle}>MonitorHídrico</Text>
            <Text style={styles.brandSubtitle}>Monitoramento de sensores</Text>
          </View>
        </View>
        <TouchableOpacity style={styles.logoutButton} onPress={logout}>
          <Icon name="sign-out" size={16} color="#00b4c6" />
          <Text style={styles.logoutText}>Sair</Text>
        </TouchableOpacity>
      </View>

      {/* Conteúdo Principal */}
      <View style={styles.mainContent}>
        <Text style={styles.welcomeTitle}>
          Olá, {user?.nome?.split(' ')[0] || 'Usuário'}!
        </Text>
        <Text style={styles.welcomeSubtitle}>
          Aqui está o resumo do seu sistema de monitoramento
        </Text>

        {/* Grid de Sensores (substitui o mapa) */}
        {renderSensorsGrid()}

        {/* Notificações */}
        <View style={styles.card}>
          <View style={styles.cardHeader}>
            <Text style={styles.cardTitle}>Notificações</Text>
            <TouchableOpacity>
              <Text style={styles.seeAllText}>Ver todos</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.notificationsList}>
            <View style={styles.notificationItem}>
              <View style={[styles.notificationIcon, styles.warnIcon]}>
                <Text style={styles.notificationIconText}>!</Text>
              </View>
              <View style={styles.notificationContent}>
                <Text style={styles.notificationTitle}>Alerta de Nível Crítico</Text>
                <Text style={styles.notificationDescription}>
                  Sensor Rio Principal — Nível: 85%
                </Text>
                <Text style={styles.notificationTime}>Há 15 minutos</Text>
              </View>
            </View>

            <View style={styles.notificationItem}>
              <View style={[styles.notificationIcon, styles.infoIcon]}>
                <Text style={styles.notificationIconText}>i</Text>
              </View>
              <View style={styles.notificationContent}>
                <Text style={styles.notificationTitle}>Novo Sensor Conectado</Text>
                <Text style={styles.notificationDescription}>
                  Sensor "Represa Leste" foi adicionado
                </Text>
                <Text style={styles.notificationTime}>Há 2 horas</Text>
              </View>
            </View>
          </View>
        </View>
      </View>

      {/* Sidebar */}
      <View style={styles.sidebar}>
        {/* Perfil */}
        <View style={styles.profileCard}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>
              {user?.nome?.charAt(0)?.toUpperCase() || 'U'}
            </Text>
          </View>
          <View style={styles.profileInfo}>
            <Text style={styles.profileName}>{user?.nome || 'Usuário'}</Text>
            <Text style={styles.profileEmail}>{user?.email || 'email@exemplo.com'}</Text>
          </View>

          <View style={styles.profileActions}>
            <TouchableOpacity 
              style={[styles.button, styles.ghostButton, styles.smallButton]}
              onPress={() => openEditModal('name')}
            >
              <Text style={styles.ghostButtonText}>Alterar Nome</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={[styles.button, styles.ghostButton, styles.smallButton]}
              onPress={() => openEditModal('password')}
            >
              <Text style={styles.ghostButtonText}>Alterar Senha</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={[styles.button, styles.dangerButton, styles.smallButton]}
              onPress={deleteAccount}
            >
              <Text style={styles.dangerButtonText}>Excluir Conta</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Sensores */}
        <View style={styles.sensorsCard}>
          <Text style={styles.cardTitle}>Sensores</Text>
          <View style={styles.sensorsInfo}>
            <View>
              <Text style={styles.sensorsLabel}>Sensores Ativos</Text>
              <Text style={styles.sensorsCount}>{activeSensorsCount}</Text>
            </View>
            <TouchableOpacity style={[styles.button, styles.ghostButton]}>
              <Text style={styles.ghostButtonText}>Ver Todos</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>

      {/* Footer */}
      <View style={styles.footer}>
        <Text style={styles.footerText}>Demo — Sistema de Monitoramento Hídrico</Text>
      </View>

      {/* Modal de Edição */}
      {renderEditModal()}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fbfd',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f8fbfd',
  },
  loadingText: {
    marginTop: 10,
    color: '#7d8c93',
    fontSize: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: '#fff',
    margin: 15,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.06,
    shadowRadius: 25,
    elevation: 5,
  },
  brand: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  dropIcon: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#00b4c6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  brandTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#0e3940',
  },
  brandSubtitle: {
    fontSize: 12,
    color: '#7d8c93',
  },
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
    borderWidth: 2,
    borderColor: 'rgba(0, 174, 191, 0.12)',
    borderRadius: 12,
  },
  logoutText: {
    color: '#00b4c6',
    fontWeight: '600',
    marginLeft: 6,
    fontSize: 12,
  },
  mainContent: {
    padding: 15,
  },
  welcomeTitle: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#112233',
    marginBottom: 5,
  },
  welcomeSubtitle: {
    fontSize: 15,
    color: '#7d8c93',
    marginBottom: 20,
  },
  heroCard: {
    backgroundColor: '#f9feff',
    borderRadius: 16,
    padding: 18,
    marginBottom: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.06,
    shadowRadius: 25,
    elevation: 5,
    borderWidth: 1,
    borderColor: 'rgba(2, 50, 60, 0.03)',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#112233',
    marginBottom: 15,
  },
  sensorsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  sensorCard: {
    width: '48%',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 15,
    marginBottom: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sensorStatus: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  sensorName: {
    fontSize: 14,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 4,
  },
  sensorLocation: {
    fontSize: 12,
    color: '#7d8c93',
    textAlign: 'center',
    marginBottom: 6,
  },
  sensorStatusText: {
    fontSize: 11,
    fontWeight: '600',
    textAlign: 'center',
  },
  mapLegend: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 15,
  },
  legendDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 6,
  },
  greenDot: {
    backgroundColor: '#7ed957',
  },
  yellowDot: {
    backgroundColor: '#ffd36b',
  },
  redDot: {
    backgroundColor: '#ff8a7a',
  },
  legendText: {
    fontSize: 13,
    color: '#7d8c93',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 18,
    marginBottom: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.06,
    shadowRadius: 25,
    elevation: 5,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#112233',
  },
  seeAllText: {
    color: '#00b4c6',
    fontSize: 14,
  },
  notificationsList: {
    marginTop: 12,
  },
  notificationItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 12,
    marginBottom: 8,
  },
  notificationIcon: {
    width: 44,
    height: 44,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  warnIcon: {
    backgroundColor: '#fff4d9',
    borderWidth: 1,
    borderColor: 'rgba(180, 123, 0, 0.08)',
  },
  infoIcon: {
    backgroundColor: '#eef6ff',
    borderWidth: 1,
    borderColor: 'rgba(31, 135, 209, 0.06)',
  },
  notificationIconText: {
    fontWeight: 'bold',
    fontSize: 16,
  },
  notificationContent: {
    flex: 1,
  },
  notificationTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  notificationDescription: {
    fontSize: 13,
    color: '#7d8c93',
    marginBottom: 2,
  },
  notificationTime: {
    fontSize: 12,
    color: '#7d8c93',
    fontStyle: 'italic',
  },
  sidebar: {
    padding: 15,
  },
  profileCard: {
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 18,
    alignItems: 'center',
    marginBottom: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.06,
    shadowRadius: 25,
    elevation: 5,
  },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#e6f5fa',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
  },
  avatarText: {
    fontSize: 30,
    color: '#00b4c6',
    fontWeight: 'bold',
  },
  profileInfo: {
    alignItems: 'center',
    marginBottom: 12,
  },
  profileName: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  profileEmail: {
    fontSize: 13,
    color: '#7d8c93',
  },
  profileActions: {
    width: '100%',
  },
  sensorsCard: {
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.06,
    shadowRadius: 25,
    elevation: 5,
  },
  sensorsInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 10,
  },
  sensorsLabel: {
    color: '#7d8c93',
    fontSize: 14,
  },
  sensorsCount: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#0c5460',
  },
  button: {
    padding: 10,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  smallButton: {
    padding: 8,
    marginBottom: 8,
  },
  primaryButton: {
    backgroundColor: '#00b4c6',
  },
  ghostButton: {
    backgroundColor: 'transparent',
    borderWidth: 2,
    borderColor: 'rgba(0, 174, 191, 0.12)',
  },
  dangerButton: {
    backgroundColor: '#ff7a7a',
  },
  primaryButtonText: {
    color: '#fff',
    fontWeight: '600',
  },
  ghostButtonText: {
    color: '#00b4c6',
    fontWeight: '600',
  },
  dangerButtonText: {
    color: '#fff',
    fontWeight: '600',
  },
  footer: {
    padding: 20,
    alignItems: 'center',
  },
  footerText: {
    color: '#7d8c93',
    fontSize: 13,
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.5)',
    padding: 20,
  },
  modalContent: {
    backgroundColor: 'white',
    borderRadius: 15,
    padding: 25,
    width: '100%',
    maxWidth: 400,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.2,
    shadowRadius: 30,
    elevation: 10,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#0e3940',
    marginBottom: 15,
  },
  input: {
    borderWidth: 2,
    borderColor: '#e9ecef',
    borderRadius: 8,
    padding: 10,
    fontSize: 14,
    marginBottom: 15,
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 20,
    gap: 10,
  },
});

