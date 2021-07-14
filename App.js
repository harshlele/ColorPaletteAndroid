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
  TouchableHighlight,
  useColorScheme,
} from 'react-native';


const App = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const styles = StyleSheet.create({
    containerStyle:{
      backgroundColor: isDarkMode ? 'black' : 'white',
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center'
    },
    pickBtnStyle: {
      width: '80%',
      backgroundColor: 'red',
      borderRadius: 30,
      paddingVertical: 20,  
    },
    btnTextStyle: {
      color: 'white',
      textAlign: 'center',
      fontSize: 18
    }
  });
  
  const onBtnPress = () => {
    console.log("btn press!");
    console.log("another log message!");
  };

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={true} />
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.5}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
    </SafeAreaView>
  );
};

export default App;
