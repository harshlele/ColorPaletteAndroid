/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useState, useRef, useEffect} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  Image,
  FlatList,
  View,
  TouchableHighlight,
  TouchableOpacity,
  Appearance,
  Animated, 
  Easing,
  NativeModules,
  NativeEventEmitter,
  ToastAndroid
} from 'react-native';
import {launchImageLibrary} from 'react-native-image-picker';
import Clipboard from '@react-native-clipboard/clipboard';
import {ntc, colorLight, colorDark, colorAccent} from './util/Color'; 

const App = () => {
  
  const { ColorPaletteModule } = NativeModules;
 
  const [currImage,setImage] = useState({});
  const dispImgW = useRef(new Animated.Value(320)).current;
  const dispImgH = useRef(new Animated.Value(320)).current;
  const [palette,setPalette] = useState([]);
  
  /* styles/animations */
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
      backgroundColor: isDarkMode ? colorDark : colorLight,
      flex: 1,
      alignItems: 'center',
      justifyContent: 'space-around'
    },
    pickBtnStyle: {
      width: '80%',
      backgroundColor: colorAccent,
      borderRadius: 30,
      paddingVertical: 20
    },
    btnTextStyle: {
      color: 'white',
      textAlign: 'center',
      fontSize: 18
    },
    imgCoverStyle: { 
      backgroundColor: 'transparent', 
      marginTop: 80, 
      marginBottom: 50,
      width: 330,
      height: 330,
      alignItems: 'center'
    },
    listItemStyle: {
      padding: 10, 
      height: 80, 
      width: 80, 
      alignItems: 'center',
      justifyContent:'center', 
      borderRadius: 40,
      borderWidth: 2,
      borderColor: isDarkMode ? colorLight : colorDark
    }
  });
  /* end of styles/animations/color */


  /**
   * sets the image component width/height according to the aspect ratio 
   * @param {number} aspRatio   aspect ratio(width/height) of image 
   */
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

  /**
   * Copies hex code to clipboard
   * @param {string} hex  color hex code 
   */
  const copyColor = (hex) => {
    Clipboard.setString(hex);
    ToastAndroid.show('Copied to clipboard!',ToastAndroid.SHORT);
  };

  /**
   * Item component for the color list
   */
  const ListItem = ({ item }) => (
    <View style={{ alignItems: 'center', justifyContent: 'center', marginVertical: 10, marginHorizontal: 10}}>
      <TouchableOpacity style={{...styles.listItemStyle,backgroundColor: item.hex}} onPress={() => {copyColor(item.hex)}}>
        <View>
          <Text style={{fontSize: 12, color: item.textColor}}>{item.hex}</Text>
        </View>
      </TouchableOpacity>
      <View style={{marginTop: 5}}>
        <Text style={{color: isDarkMode ? colorLight : colorDark}}>{item.name}</Text>
      </View>
    </View>
  );


  /**
   * First load hook
   */
  useEffect(() => {
    //init ntc library
    ntc.init();

    //listen for events from native module
    const eventEmitter = new NativeEventEmitter(NativeModules.ColorPaletteModule);
    const paletteListener = eventEmitter.addListener('paletteGen',(event) => {
      let palette = event.palette.map((c,i) => {
        let hex = ntc.rgbToHex(c);
        let hsl = ntc.hsl(hex);

        return {
          ...c,
          h: hsl[0],
          s: hsl[1],
          l: hsl[2],
          hex: hex.toUpperCase(),
          key: `color-${i}`,
          textColor: ntc.getTextColor(c),
          name: ntc.name(hex)[1]
        }
      });

      setPalette(palette);
    });

    const errorListener = eventEmitter.addListener('error',(event) => {
      console.log(event.msg);
    });

    return () => {
      paletteListener.remove();
      errorListener.remove();
    };
  },[]);

  /**
   * pick image button click listener
   */
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

          setImage({uri: img.uri});
          //width/height values from Image are more accurate (esp pics taken with the camera)
          Image.getSize(img.uri,(width,height) => {
            setImgSize(width/height);
          }, (error) => {
            console.log(error);
            setImgSize(aspRatio);
          });
  
          setPalette([]);
          ColorPaletteModule.getColorPalette(img.uri);
        }
      }
    });
  };

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar backgroundColor={styles.containerStyle.backgroundColor} barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={false} />
      <View style={styles.imgCoverStyle}>
        <View style={{borderWidth: 5, borderRadius: 10, borderColor: currImage.uri ? colorAccent : 'transparent'}}>
          <Animated.Image style={{ width: dispImgW, height: dispImgH, borderRadius: 7}} source={currImage} resizeMode='contain'></Animated.Image>
        </View>  
      </View> 
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.8} underlayColor={styles.containerStyle.backgroundColor}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
      <FlatList
        data={palette}
        renderItem={ListItem}
        keyExtractor={(item) => item.key}
        horizontal={true}
        style={{width: '100%', marginTop: 20}}
      >
      </FlatList>
    </SafeAreaView>
  );
};

export default App;
