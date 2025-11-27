import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
  Modal
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';

const LoginPage = ({ navigation }) => {
  const [activeTab, setActiveTab] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [name, setName] = useState('');
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');

  const API_BASE_URL = "http://192.168.3.32:8081/api";

  const handleLogin = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          senha: password
        })
      });

      const result = await response.json();

      if (response.ok) {
        // Salvar token e redirecionar
        navigation.navigate('Dashboard');
        Alert.alert('Sucesso', 'Login realizado com sucesso!');
      } else {
        Alert.alert('Erro', result.message || 'Erro ao fazer login');
      }
    } catch (error) {
      Alert.alert('Erro', 'Erro de conexão. Tente novamente.');
    }
  };

  const handleRegister = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nome: name,
          email: email,
          senha: password,
          telefone: ''
        })
      });

      const result = await response.json();

      if (response.ok) {
        Alert.alert('Sucesso', 'Cadastro realizado com sucesso!');
        setActiveTab('login');
      } else {
        Alert.alert('Erro', result.message || 'Erro ao criar conta');
      }
    } catch (error) {
      Alert.alert('Erro', 'Erro de conexão. Tente novamente.');
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <LinearGradient
        colors={['#0d3b66', '#1e5a96']}
        style={styles.background}
      >
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <View style={styles.card}>
            {/* Welcome Section */}
            <LinearGradient
              colors={['#0d3b66', '#1e5a96']}
              style={styles.welcomeSection}
            >
              <View style={styles.logo}>
                <View style={styles.logoIcon}>
                  <Ionicons name="water" size={24} color="white" />
                </View>
                <Text style={styles.logoText}>MonitorHídrico</Text>
              </View>

              <Text style={styles.welcomeTitle}>
                Monitoramento Inteligente de Recursos Hídricos
              </Text>
              <Text style={styles.welcomeSubtitle}>
                Sistema completo para monitoramento em tempo real de níveis de água
              </Text>
            </LinearGradient>

            {/* Auth Section */}
            <View style={styles.authSection}>
              <View style={styles.authTabs}>
                <TouchableOpacity
                  style={[styles.authTab, activeTab === 'login' && styles.activeTab]}
                  onPress={() => setActiveTab('login')}
                >
                  <Text style={[styles.tabText, activeTab === 'login' && styles.activeTabText]}>
                    Entrar
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.authTab, activeTab === 'register' && styles.activeTab]}
                  onPress={() => setActiveTab('register')}
                >
                  <Text style={[styles.tabText, activeTab === 'register' && styles.activeTabText]}>
                    Cadastrar
                  </Text>
                </TouchableOpacity>
              </View>

              {activeTab === 'login' && (
                <View style={styles.form}>
                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>E-mail</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="seu@email.com"
                      value={email}
                      onChangeText={setEmail}
                      keyboardType="email-address"
                      autoCapitalize="none"
                    />
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Senha</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="Sua senha"
                      value={password}
                      onChangeText={setPassword}
                      secureTextEntry
                    />
                  </View>

                  <TouchableOpacity style={styles.primaryButton} onPress={handleLogin}>
                    <Text style={styles.buttonText}>Entrar</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.forgotPassword}
                    onPress={() => setShowForgotPassword(true)}
                  >
                    <Text style={styles.forgotPasswordText}>Esqueceu sua senha?</Text>
                  </TouchableOpacity>
                </View>
              )}

              {activeTab === 'register' && (
                <View style={styles.form}>
                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Nome Completo</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="Seu nome completo"
                      value={name}
                      onChangeText={setName}
                    />
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>E-mail</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="seu@email.com"
                      value={email}
                      onChangeText={setEmail}
                      keyboardType="email-address"
                      autoCapitalize="none"
                    />
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Senha</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="Mínimo 6 caracteres"
                      value={password}
                      onChangeText={setPassword}
                      secureTextEntry
                    />
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Confirmar Senha</Text>
                    <TextInput
                      style={styles.input}
                      placeholder="Digite novamente"
                      value={confirmPassword}
                      onChangeText={setConfirmPassword}
                      secureTextEntry
                    />
                  </View>

                  <TouchableOpacity style={styles.primaryButton} onPress={handleRegister}>
                    <Text style={styles.buttonText}>Criar Conta</Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          </View>
        </ScrollView>
      </LinearGradient>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  background: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 20,
  },
  card: {
    backgroundColor: 'white',
    borderRadius: 20,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 15,
    },
    shadowOpacity: 0.2,
    shadowRadius: 50,
    elevation: 10,
  },
  welcomeSection: {
    padding: 30,
  },
  logo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 30,
  },
  logoIcon: {
    width: 50,
    height: 50,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 25,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  logoText: {
    color: 'white',
    fontSize: 24,
    fontWeight: '700',
  },
  welcomeTitle: {
    color: 'white',
    fontSize: 24,
    fontWeight: '600',
    marginBottom: 10,
    lineHeight: 30,
  },
  welcomeSubtitle: {
    color: 'rgba(255, 255, 255, 0.9)',
    fontSize: 16,
    lineHeight: 22,
  },
  authSection: {
    padding: 30,
  },
  authTabs: {
    flexDirection: 'row',
    borderBottomWidth: 2,
    borderBottomColor: '#eee',
    marginBottom: 30,
  },
  authTab: {
    flex: 1,
    padding: 15,
    alignItems: 'center',
  },
  activeTab: {
    borderBottomWidth: 3,
    borderBottomColor: '#0d3b66',
  },
  tabText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#6c757d',
  },
  activeTabText: {
    color: '#0d3b66',
  },
  form: {
    width: '100%',
  },
  inputGroup: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#343a40',
    marginBottom: 8,
  },
  input: {
    borderWidth: 2,
    borderColor: '#e9ecef',
    borderRadius: 10,
    padding: 15,
    fontSize: 16,
  },
  primaryButton: {
    backgroundColor: '#0d3b66',
    borderRadius: 10,
    padding: 15,
    alignItems: 'center',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
  forgotPassword: {
    alignItems: 'center',
    marginTop: 15,
  },
  forgotPasswordText: {
    color: '#00aebf',
    fontSize: 14,
  },
});

export default LoginPage;