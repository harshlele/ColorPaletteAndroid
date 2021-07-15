/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  Image,
  TouchableHighlight,
  Appearance,
} from 'react-native';


const App = () => {
  const col = Appearance.getColorScheme();
  const isDarkMode =  col === 'dark';

  const styles = StyleSheet.create({
    containerStyle:{
      backgroundColor: isDarkMode ? '#002b36' : '#fdf6e3',
      flex: 1,
      alignItems: 'center'
    },
    pickBtnStyle: {
      width: '80%',
      backgroundColor: '#dc322f',
      borderRadius: 30,
      paddingVertical: 20,  
      marginBottom: 10,
    },
    btnTextStyle: {
      color: 'white',
      textAlign: 'center',
      fontSize: 18
    }
  });
  
  const onBtnPress = () => {
    console.log('btn press!');
    console.log('another log message!');
  };

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar backgroundColor={styles.containerStyle.backgroundColor} barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={false} />
      <Image style={{height: 400}}></Image>
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.8} underlayColor={styles.containerStyle.backgroundColor}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
    </SafeAreaView>
  );
};

export default App;
