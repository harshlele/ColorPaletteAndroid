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
};

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
};

/**
 * 
 * @param {Array} colorArr  color array
 */
ntc.genSmallPalette = (colorArr) => {

    let satRank = [...Array(colorArr.length).keys()];
    let lumRank = [...Array(colorArr.length).keys()];
    let sizeRank = [...Array(colorArr.length).keys()];
    let fRank = [...Array(colorArr.length).keys()];

    satRank.sort((i1,i2) => (colorArr[i2].s) - (colorArr[i1].s));
    lumRank.sort((i1,i2) => Math.abs(colorArr[i1].l - 127) - Math.abs(colorArr[i2].l - 127));
    sizeRank.sort((i1,i2) => i2.cSize - i1.cSize);

    fRank.sort((e1,e2) => {
        let f1 = (satRank.indexOf(e1) + 1) * (lumRank.indexOf(e1) + 1) * (sizeRank.indexOf(e1) + 1);
        let f2 = (satRank.indexOf(e2) + 1) * (lumRank.indexOf(e2) + 1) * (sizeRank.indexOf(e2) + 1);
        return f1 - f2;
    });

    let sArr = fRank.map(e => colorArr[e]);

    return sArr;

};

module.exports = {ntc,colorLight,colorDark,colorAccent}
