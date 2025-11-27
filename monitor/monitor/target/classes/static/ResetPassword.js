import React, { useState } from "react";
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    Alert,
    ActivityIndicator,
    KeyboardAvoidingView,
    Plataform,
    ScrollView,
    Dimensions
} from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';
import AsyncStorage from '@react-native-async-storage/async-storage';

const { width, height } = Dimensions.get('window');
const API_BASE_URL = "http://192.168.3.32:8081/api";

export default function ResetPassword({ navigation, route }) {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [passwordStrength, setPasswordStrength] = useState(0);

    const token = route.param?.token;

    if (!token) {
        Alert.alert('Erro', 'Token não encontrado');
        navigation.goBack();
    }

    const calculatePasswordStrength = (password) => {
        let strength = 0;
        if (password.length >= 6) strength += 25;
        if (password.length >= 8) strength += 25;
        if (/[A-Z]/.test(password)) strength += 25;
        if (/[0-9]/.test(password)) strength += 25;
        return strength
    };

    const handlePasswordChange = (password) => {
        setNewPassword(password);
        setPasswordStrength(calculatePasswordStrength(password));
    };

    const getPasswordStrengthColor = () => {
        if (passwordStrength < 50) return '#ff4757';
        if (passwordStrength < 75) return '#ffa502';
        return '#2ed573';
    };

    const getPsswordStrengthText = () => {
        if (passwordStrength < 50) return 'Fraca';
        if (passwordStrength < 75) return 'Média';
        return 'Forte';
    };


    const handleResetPassword = async () => {
        if (!newPassword || !confirmPassword) {
            Alert.alert('Erro', 'Por favor, preencha todos os campos');
            return;
        }

        if (newPassword.length < 6) {
            Alert.alert('Erro', 'A senha deve ter pelo menos 6 caracteres');
            return;
        }

        if (newPassword !== confirmPassword) {
            Alert.alert('Erro', 'As senhas não coincidem');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    token: token,
                    newPassword: newPassword,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                Alert.alert(
                    'Sucesso',
                    'Senha redefinida com sucesso!',
                    [
                        {
                            text: 'OK',
                            onPress: () => navigation.navigate('Login')
                        }
                    ]
                );
            } else {
                Alert.alert('Erro', data.message || 'Erro ao redefinir senha');
            }
        } catch (error) {
            console.error('Erro no rset password', error);
            Alert.alert('Erro', 'Não foi possivel conectar ao servidor');
        } finally {
            setLoading(false);
        };

        return (
            <KeyboardAvoidingView
                style={StyleSheet.container}
                behavior={Plataform.OS === 'ios' ? 'padding' : 'heigth'}>
                <ScrollView
                    contentContainerStyle={StyleSheet.scrollContainer}
                    showsVerticalScrollIndicator={false}>
                    <View style={styles.content}>
                        {/* Header */}
                        <View style={styles.header}>
                            <View style={styles.logoContainer}>
                                <View style={styles.logoIcon}>
                                    <Icon name="tint" size={32} color="#fff" />
                                </View>
                                <Text style={styles.logoText}>MonitorHídrico</Text>
                            </View>
                            <Text style={styles.subtitle}>Reefinir Senha</Text>
                        </View>

                        {/* Formulário */}
                        <View style={styles.formContainer}>
                            <Text style={styles.title}>Criar Nova Senha</Text>
                            <Text style={style.instruction}>
                                Digite sa nova senha abaixo
                            </Text>

                            {/* Nova Senha */}
                            <View style={styles.inputContainer}>
                                <Icon name="lock" size={18} color="#7d8c93" style={styles.inputIcon} />
                                <TextInput
                                    style={styles.input}
                                    placeholder="Nova Senha"
                                    placeholderTextColor="#9ca3af"
                                    value={newPassword}
                                    onChangeText={handlePasswordChange}
                                    secureTextEntry={!showNewPassword} />
                                <TouchableOpacity
                                    style={styles.eyeIcon}
                                    onPress={() => setShowNewPassword(!showNewPassword)}>

                                    <Icon
                                        name={showNewPassword ? 'eye-slash' : 'eye'}
                                        size={16}
                                        color="#7d8c93" />
                                </TouchableOpacity>
                            </View>

                            {/* Indicador de força de senha */}
                            {newPassword.length > 0 && (
                                <View style={styles.passwordStrengthContainer}>
                                    <View style={styles.passwordStrengthBar}>
                                        <View
                                            style={[
                                                styles.passwordStrengthFill,
                                                {
                                                    width: `$(passwordStrength)%`,
                                                    backgroundColor: getPasswordStrengthColor()
                                                }
                                            ]}
                                        />
                                    </View>
                                    <Text style={styles.passwordStrengthText}>
                                        Força: {getPasswordStrengthText()}
                                    </Text>
                                </View>
                            )}

                            {/* Confirmar Senha */}
                            <View style={styles.inputContainer}>
                                <Icon name="lock" size={18} color="#7d8c93" style={styles.inputIcon}/>
                                <TextInput
                                style={styles.input}
                                placeholder="Confirmar Nova Senha"
                                placeholderTextColor="#9ca3af"
                                value={confirmPassword}
                                onChangeText={setConfirmPassword}
                                secureTextEntry={!showConfirmPassword} />
                                <TouchableOpacity
                                style={styles.eyeIcon}
                                onPress={() => setShowConfirmPassword(!showConfirmPassword)}>
                                    <Icon
                                    name={showConfirmPassword ? 'eye-slash' : 'eye'}
                                    size={16}
                                    color="#7d8c93"/>
                                </TouchableOpacity>
                            </View>

                            {/** Botão de Redefinir */}
                            <TouchableOpacity
                            style={[styles.resetButton, loading && styles.resetButtonDisabled]}
                            onPress={handleResetPassword}
                            disabled={loading}>
                                {loading ? (
                                    <ActivityIndicator size="small" color="#fff"/>
                                ) : (
                                    <>
                                    <Text style={styles.resetButtonText}>Redefinir Senha</Text>
                                    <Icon name="check" size={16} color="#fff"/>
                                    </>
                                )}
                            </TouchableOpacity>

                            {/* Link para voltar ao login */}
                            <TouchableOpacity
                            style={styles.backLink}
                            onPress={() => navigation.navigate('Login')}>
                                <Icon name="arrow-left" size={14} color="#00b4c6"/>
                                <Text style={styles.backLinkText}>Voltar para o login</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </ScrollView>
            </KeyboardAvoidingView>
        );
    }

    const styles = StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: '#f8fbfd',
        },
        contedt: {
            padding: 25,
        },
        header: {
            alignItems: 'center',
            marginBottom: 40,
        },
        logoContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            marginBottom: 10,
        },
        logoIcon: {
            width: 50,
            height: 50,
            borderRadius: 25,
            backgroundColor: '#00b4c6',
            justifyContent: 'center',
            alignItems: 'center',
            marginRigth: 12,
            shadowColor: '#00b3c6',
            shadowOffset: { width: 0, heigth: 4 },
            shadowOpacity: 0.3,
            shadowRadius: 8,
            elevation: 5,
        },
        logoText: {
            fontSize: 28,
            color: '#7d8c93',
            textAlign: 'center',
        },
        formContainer: {
            backgroundColor: '#fff',
            borderRaius: 20,
            padding: 25,
            shadowColor: '#000',
            shadowOffset: { width: 0, heigth: 10 },
            shadowOpacity: 0.06,
            shadowRadius: 25,
            elevation: 5,
        },
        title: {
            fontSize: 22,
            fontWeight: 'bold',
            color: '#0e3940',
            marginBottom: 8,
            textAlign: 'center',
        },
        instruction: {
            fontSize: 14,
            color: '#7d8c93',
            marginBottom: 30,
            textAlign: 'center',
            lineHeight: 20,
        },
        inputContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            borderWidth: 2,
            borderColor: '#e9ecef',
            borderRadius: 12,
            marginBottom: 16,
            paddingHorizontal: 15,
            backgroundColor: '#fafbfc',
        },
        inputIcon: {
            marginRigth: 12,
        },
        input: {
            flex: 1,
            paddingVertical: 15,
            fontSize: 16,
            color: '#0e3940',
        },
        eyeIcon: {
            padding: 9,
        },
        passwordStrengthContainer: {
            marginBottom: 20,
        },
        passwordStrengthBar: {
            heigth: 6,
            backgroundColor: '#e9ccef',
            borderRadius: 3,
            overflow: 'hidden',
            marginBottom: 5,
        },
        passwordStrengthFill: {
            heigth: '100%',
            borderRadius: 3,
            trasition: 'all 0.3s ease',
        },
        passwordStrengthText: {
            fontSize: 12,
            color: '#7d8c93',
            textAlign: 'right',
        },
        resetButton: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: '#00b4c6',
            shadowOffset: { width: 0, height: 4 },
            shadowOpacity: 0.3,
            shadowRadius: 8,
            elevation: 5,
        },
        resetButtonDisabled: {
            opacity: 0.7,
       },
       resetButtonText: {
           color: '#fff'
           fontSize: 16,
           fontWeight: '600',
           marginRigth: 8,
       },
       backLink: {
       flexDirection: 'row',
       alignItems: 'center',
       justifyContent: 'center',
       padding: 20,
       },
       backLinkText: {
       color: '#00b4c6',
       fontSize: 14,
       fontWeight: '500',
       marginLeft: 8,
       },
    });