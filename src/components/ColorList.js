import React from 'react';
import { FlatList, View, Text, TouchableOpacity } from 'react-native';
import {colorLight, colorDark} from '../../util/Color'; 

/**
 * Color list
 * @param {Object} props
 * Props: 
 * translucent {boolean}    change opacity of list
 * listItemStyle {Object}   style of the list items
 * onColorPress {Function}  callback for when a list item is pressed
 * colorList {Array}        list data  
 * isDarkMode {boolean}     true if dark mode is on
 * @returns ColorList component
 */
export default function ColorList(props){

  /**
  * Item component for the color list
  */
  const ListItem = ({ item }) => (
    <View style={{ alignItems: 'center', justifyContent: 'center', marginVertical: 10, marginHorizontal: 10}}>
    <TouchableOpacity style={{...props.listItemStyle,backgroundColor: item.hex}} onPress={() => {props.onColorPress(item)}}>
      <View>
      <Text style={{fontSize: 12, color: item.textColor}}>{item.hex}</Text>
      </View>
    </TouchableOpacity>
    <View style={{marginTop: 5}}>
      <Text style={{color: props.isDarkMode ? colorLight : colorDark}}>{item.name}</Text>
    </View>
    </View>
  );


  return (
    <FlatList
    data={props.colorList}
    renderItem={ListItem}
    keyExtractor={(item) => item.key}
    horizontal={true}  
    style={{zIndex: 0, opacity: props.translucent ? 0.3 : 1}}
    > 
    </FlatList>
  )


}