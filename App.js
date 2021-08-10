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
  View,
  TouchableHighlight,
  Appearance,
  Animated, 
  Easing,
  NativeModules,
  NativeEventEmitter,
  ToastAndroid,
  PermissionsAndroid
} from 'react-native';
import {launchImageLibrary} from 'react-native-image-picker';
import Clipboard from '@react-native-clipboard/clipboard';
import LottieView from 'lottie-react-native';

import {ntc, colorLight, colorDark, colorAccent} from './util/Color'; 
import ColorList from './src/components/ColorList';

const App = () => {
  
  const { ColorPaletteModule } = NativeModules;
 
  const [currImage,setImage] = useState({});
  const dispImgW = useRef(new Animated.Value(320)).current;
  const dispImgH = useRef(new Animated.Value(320)).current;
  const [palette,setPalette] = useState([]);
  const [loading, setLoading] = useState(false);
  
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
   * Checks if the app has read storage permission (and asks for the permission if it doesn't)
   * @returns {Promise} promise that fulfills with a status of whether the app has the storage permission or not
   */
  const checkStoragePermission = () => {
    return new Promise(async (resolve) => {
      let status = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE);      
      if(status == PermissionsAndroid.RESULTS.GRANTED) resolve(true);
      else{
        let askPerm = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE);
        if(askPerm == PermissionsAndroid.RESULTS.GRANTED) resolve(true);
        else resolve(false);
      }
    });
  };

  
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
          name: ntc.name(hex)[1],
          cSize: event.sizes[i]
        }
      });

      if(event.final) {
        let sArr = ntc.genSmallPalette(palette);
        setPalette(sArr);
        setLoading(false);
      }
      else setPalette(palette);
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
  const onBtnPress = async () => {

    if(loading) return;

    const permission = await checkStoragePermission();
    if(!permission){
      ToastAndroid.show('Error while checking for storage permission, please grant the app storage permissions through the settings', ToastAndroid.LONG);
      return;
    }

    launchImageLibrary({
      mediaType: 'photo',
      quality: 1,
    },(resp) => {
      if(!resp.didCancel){
        if(resp.errorCode) console.log(resp.errorMessage);
        else{
          let img = resp.assets[0];
          let aspRatio = img.width/img.height;

          setPalette([]);
          setLoading(true);

          setImage({uri: img.uri});
          //width/height values from Image are more accurate (esp pics taken with the camera)
          Image.getSize(img.uri,(width,height) => {
            setImgSize(width/height);
          }, (error) => {
            console.log(error);
            setImgSize(aspRatio);
          });
          
          ColorPaletteModule.getColorPalette(img.uri);  
        }
      }
    });
  };

  /**
   * returns the imageview
   * @returns   ImageView if image has been picked, or an image animation if not
   */
  const currImageView = () => {
    if(!currImage.uri){
      return (<LottieView source={require('./assets/anim/image.json')} autoPlay loop style={{zIndex: 1}}/>);
    }
    else{  
      return (<View style={{borderWidth: 5, borderRadius: 10, borderColor: currImage.uri ? colorAccent : 'transparent'}}>
              <Animated.Image style={{ width: dispImgW, height: dispImgH, borderRadius: 7}} source={currImage} resizeMode='contain'></Animated.Image>
            </View>);  
    
    }
  }

  return (
    <SafeAreaView style={styles.containerStyle}>
      <StatusBar backgroundColor={styles.containerStyle.backgroundColor} barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent={false} />
      <View style={styles.imgCoverStyle}>
        {
          currImageView()
        }
      </View> 
      <TouchableHighlight style={styles.pickBtnStyle} onPress={onBtnPress} activeOpacity={0.8} underlayColor={styles.containerStyle.backgroundColor}>
        <Text style={styles.btnTextStyle}>Pick Image</Text>
      </TouchableHighlight>
      <View
        style={{width: '100%', height: 124, marginTop: 20}}
      >
        {
          loading && <LottieView source={require('./assets/anim/loading-animation.json')} autoPlay loop style={{zIndex: 1}}/>
        }
        <ColorList colorList={palette} onColorPress={(item) => {copyColor(item.hex)}} listItemStyle={styles.listItemStyle} translucent={loading} isDarkMode={isDarkMode}/>
      </View>
    </SafeAreaView>
  );
};

export default App;
