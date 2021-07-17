/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useState, useRef} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  Image,
  View,
  TouchableHighlight,
  Appearance,
  Animated, Easing
} from 'react-native';

import {launchImageLibrary} from 'react-native-image-picker';


const App = () => {
  
  const [currImage,setImage] = useState({});
  const dispImgW = useRef(new Animated.Value(320)).current;
  const dispImgH = useRef(new Animated.Value(320)).current;
  
  const duration = 200;
  const easing = Easing.linear;

  const animImgWidth = (newWidth) => {
    Animated.timing(dispImgW,{
      toValue: newWidth,
      useNativeDriver: false,
      duration,
      easing
    }).start();
  };

  const animImgHeight = (newHeight) => {
    Animated.timing(dispImgH,{
      toValue: newHeight,
      useNativeDriver: false,
      duration,
      easing
    }).start();
  };

  const isDarkMode =  Appearance.getColorScheme() === 'dark';
  
  const styles = StyleSheet.create({
    containerStyle:{
      backgroundColor: isDarkMode ? '#002b36' : '#fdf6e3',
      flex: 1,
      alignItems: 'center',
      justifyContent: 'space-between'
    },
    pickBtnStyle: {
      width: '80%',
      backgroundColor: '#dc322f',
      borderRadius: 30,
      paddingVertical: 20,  
      marginBottom: 200,
    },
    btnTextStyle: {
      color: 'white',
      textAlign: 'center',
      fontSize: 18
    },
    imgCoverStyle: { 
      padding: 5, 
      backgroundColor: currImage.uri ? '#dc322f' : 'transparent', 
      marginTop: 80, 
      marginBottom: 50, 
      borderRadius: 10
    }
  });

  const setImgSize = (aspRatio) => {
    if(aspRatio > 1){
      animImgWidth(320);
      animImgHeight(320/aspRatio);
    }
    else{
      animImgHeight(320);
      animImgWidth(320 * aspRatio);
    }
  };
  
  const onBtnPress = () => {
    launchImageLibrary({
      mediaType: 'photo',
      quality: 1,
    },(resp) => {
      if(!resp.didCancel){
        if(resp.errorCode) console.log(resp.errorMessage);
        else{
          let img = resp.assets[0];
          let aspRatio = img.width/img.height;

          /*
           width/height values from Image are more accurate (esp pics taken with the camera)
          */
          Image.getSize(img.uri,(width,height) => {
            setImgSize(width/height);
          }, (error) => {
            console.log(error);
            setImgSize(aspRatio);
          });
          setImage({uri: img.uri});
        }
      }
    });
  };

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar backgroundColor={styles.containerStyle.backgroundColor} barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={false} />
      <View style={styles.imgCoverStyle}>
        <Animated.Image style={{ width: dispImgW, height: dispImgH, borderRadius: 5}} source={currImage} resizeMode='contain'></Animated.Image>
      </View> 
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.8} underlayColor={styles.containerStyle.backgroundColor}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
    </SafeAreaView>
  );
};

export default App;
