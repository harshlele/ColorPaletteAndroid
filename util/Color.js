/**
 * the ntc library as a module, with a few added functions, and constants
 */
import {ntc} from './ntc';

const colorLight = '#fdf6e3'
const colorDark = '#002b36';
const colorAccent = '#dc322f';

/**
 * Converts RGB to hex
 * @param {Object} color     color in RGB
 * @returns string           hex value     
 */
ntc.rgbToHex = (color) => {
    return `#${color.r.toString(16).padStart(2,'0')}${color.g.toString(16).padStart(2,'0')}${color.b.toString(16).padStart(2,'0')}`.toUpperCase();
}

/**
 * calculates and returns the text color appropriate for a given background color
 * @see from https://awik.io/determine-color-bright-dark-using-javascript/
 * @param {Object} color    color in RGB
 * @returns string          text color            
 */
ntc.getTextColor = (color) => {
    let hsp = Math.sqrt(0.299 * (color.r * color.r) + 0.587 * (color.g * color.g) + 0.114 * (color.b * color.b));
    
    if (hsp<=127.5) return colorLight; 
    else return colorDark;
}

module.exports = {ntc,colorLight,colorDark,colorAccent}
