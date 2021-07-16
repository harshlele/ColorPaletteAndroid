/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useState} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  Image,
  View,
  TouchableHighlight,
  Appearance,
} from 'react-native';

import {launchImageLibrary} from 'react-native-image-picker';


const App = () => {
  
  const [currImage,setImage] = useState({});
  
  
  const isDarkMode =  Appearance.getColorScheme() === 'dark';
  
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
    launchImageLibrary({
      mediaType: 'photo',
      quality: 1,
    },(resp) => {
      if(!resp.didCancel){
        if(resp.errorCode) console.log(resp.errorMessage);
        else{
          setImage(resp.assets[0]);
        }
      }
    });
  };

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar backgroundColor={styles.containerStyle.backgroundColor} barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={false} />
      <View style={{width: 320, height: 320, backgroundColor:'red', marginTop: 80, marginBottom: 50}}>
        <Image style={{ width: 320, height: 320}} source={currImage} resizeMode="contain"></Image>
      </View>
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.8} underlayColor={styles.containerStyle.backgroundColor}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
    </SafeAreaView>
  );
};

export default App;
