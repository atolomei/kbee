/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 8);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports) {

module.exports = "<div id=\"img-map-canvas-container\">\n  <canvas id=\"img-map-canvas\">\n    Please upgrade your browser to display this image.\n  </canvas>\n</div>\n<div id=\"img-map-actions\">\n  <span class=\"img-map-selection\">\n    <span class=\"img-map-shape-select\">\n      <input type=\"radio\" name=\"shapeSelect\" id=\"shape1\" value=\"circle\" />\n      <label for=\"shape1\">Circle</label>\n    </span>\n    <span class=\"img-map-shape-select\">\n      <input type=\"radio\" name=\"shapeSelect\" id=\"shape2\" value=\"rectangle\" />\n      <label for=\"shape2\">Rectangle</label>\n    </span>\n    <span class=\"img-map-shape-select\">\n      <input type=\"radio\" name=\"shapeSelect\" id=\"shape3\" value=\"polygon\" />\n      <label for=\"shape3\">Polygon</label>\n    </span>\n  </span>\n  <span class=\"img-map-buttons\">\n    <button type=\"button\" class=\"img-map-btn\" onclick=\"tinymceImageMap.clearCanvas(true)\">Clear</button>\n    <button type=\"button\" class=\"img-map-btn\" onclick=\"tinymceImageMap.deleteMap()\">Delete Focused</button>\n  </span>\n</div>\n<form id=\"img-map-form\">\n  <label for=\"url\">URL: </label>\n  <input\n    type=\"text\"\n    id=\"map-url-input\"\n    name=\"map-url-input\"\n    class=\"img-map-url-input\"\n    placeholder=\"https://www.validurl.com\"\n  />\n</form>\n<div id=\"img-map-hint\">Hold Shift and left click to finish drawing the polygon</div>\n";

/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {


var content = __webpack_require__(2);

if(typeof content === 'string') content = [[module.i, content, '']];

var transform;
var insertInto;



var options = {"hmr":true}

options.transform = transform
options.insertInto = undefined;

var update = __webpack_require__(6)(content, options);

if(content.locals) module.exports = content.locals;

if(false) {}

/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(3)(false);
// Imports
var urlEscape = __webpack_require__(4);
var ___CSS_LOADER_URL___0___ = urlEscape(__webpack_require__(5));

// Module
exports.push([module.i, "#img-map-container {\n  display: flex;\n  flex-direction: column;\n  overflow: scroll;\n}\n\n#img-map-canvas-container{\n  display:flex;\n  justify-content: center;\n  max-height: 800px;\n  overflow: scroll;\n}\n\n#img-map-canvas {\n  border: 1px solid #AAAAAA;\n  align-self: flex-start;\n}\n\n#img-map-actions, #img-map-form, #img-map-hint {\n  display: flex;\n  align-items: center;\n  padding: 8px 0 0 3px;\n  height: auto;\n  line-height: 20px;\n}\n\n#img-map-actions {\n  justify-content: space-between;\n}\n\n#img-map-actions .img-map-btn {\n  height: 28px;\n  border: 1px solid #b3b3b3;\n  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.75);\n  padding: 4px 6px;\n  cursor: pointer;\n  box-sizing: border-box;\n}\n\n#img-map-actions .img-map-shape-select {\n  padding-right: .8px;\n}\n\n.img-map-shape-select input[type=radio] {\n  margin: 0 auto;\n}\n\n#img-map-form .img-map-url-input {\n  width: 100%;\n  margin-left: 16px;\n  padding: 5px 6px;\n  border: 1px solid #b3b3b3;\n}\n\ni.mce-i-img-map-icon, i.mce-ico.mce-i-img-map-icon {\n  background-image: url(" + ___CSS_LOADER_URL___0___ + ");\n  max-height: 16px;\n  max-width: 16px;\n  margin-right: 4px;\n  padding-right: 0;\n}\n\n::placeholder {\n  color: #c3c3c3;\n}\n\n#img-map-hint {\n  visibility: hidden;\n  font-weight: bold;\n}", ""]);



/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/*
  MIT License http://www.opensource.org/licenses/mit-license.php
  Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function (useSourceMap) {
  var list = []; // return the list of modules as css string

  list.toString = function toString() {
    return this.map(function (item) {
      var content = cssWithMappingToString(item, useSourceMap);

      if (item[2]) {
        return '@media ' + item[2] + '{' + content + '}';
      } else {
        return content;
      }
    }).join('');
  }; // import a list of modules into the list


  list.i = function (modules, mediaQuery) {
    if (typeof modules === 'string') {
      modules = [[null, modules, '']];
    }

    var alreadyImportedModules = {};

    for (var i = 0; i < this.length; i++) {
      var id = this[i][0];

      if (id != null) {
        alreadyImportedModules[id] = true;
      }
    }

    for (i = 0; i < modules.length; i++) {
      var item = modules[i]; // skip already imported module
      // this implementation is not 100% perfect for weird media query combinations
      // when a module is imported multiple times with different media queries.
      // I hope this will never occur (Hey this way we have smaller bundles)

      if (item[0] == null || !alreadyImportedModules[item[0]]) {
        if (mediaQuery && !item[2]) {
          item[2] = mediaQuery;
        } else if (mediaQuery) {
          item[2] = '(' + item[2] + ') and (' + mediaQuery + ')';
        }

        list.push(item);
      }
    }
  };

  return list;
};

function cssWithMappingToString(item, useSourceMap) {
  var content = item[1] || '';
  var cssMapping = item[3];

  if (!cssMapping) {
    return content;
  }

  if (useSourceMap && typeof btoa === 'function') {
    var sourceMapping = toComment(cssMapping);
    var sourceURLs = cssMapping.sources.map(function (source) {
      return '/*# sourceURL=' + cssMapping.sourceRoot + source + ' */';
    });
    return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
  }

  return [content].join('\n');
} // Adapted from convert-source-map (MIT)


function toComment(sourceMap) {
  // eslint-disable-next-line no-undef
  var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
  var data = 'sourceMappingURL=data:application/json;charset=utf-8;base64,' + base64;
  return '/*# ' + data + ' */';
}

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function escape(url, needQuotes) {
  if (typeof url !== 'string') {
    return url;
  } // If url is already wrapped in quotes, remove them


  if (/^['"].*['"]$/.test(url)) {
    url = url.slice(1, -1);
  } // Should url be wrapped?
  // See https://drafts.csswg.org/css-values-3/#urls


  if (/["'() \t\n]/.test(url) || needQuotes) {
    return '"' + url.replace(/"/g, '\\"').replace(/\n/g, '\\n') + '"';
  }

  return url;
};

/***/ }),
/* 5 */
/***/ (function(module, exports) {

module.exports = "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiA/PjxzdmcgZmlsbD0ibm9uZSIgaGVpZ2h0PSIyNCIgc3Ryb2tlPSIjNTk1OTU5IiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIHN0cm9rZS13aWR0aD0iMiIgdmlld0JveD0iMCAwIDI0IDI0IiB3aWR0aD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBvbHlnb24gcG9pbnRzPSIxIDYgMSAyMiA4IDE4IDE2IDIyIDIzIDE4IDIzIDIgMTYgNiA4IDIgMSA2Ii8+PGxpbmUgeDE9IjgiIHgyPSI4IiB5MT0iMiIgeTI9IjE4Ii8+PGxpbmUgeDE9IjE2IiB4Mj0iMTYiIHkxPSI2IiB5Mj0iMjIiLz48L3N2Zz4KCjwhLS0KClRoZSBNSVQgTGljZW5zZSAoTUlUKQoKQ29weXJpZ2h0IChjKSAyMDEzLTIwMTcgQ29sZSBCZW1pcwoKUGVybWlzc2lvbiBpcyBoZXJlYnkgZ3JhbnRlZCwgZnJlZSBvZiBjaGFyZ2UsIHRvIGFueSBwZXJzb24gb2J0YWluaW5nIGEgY29weQpvZiB0aGlzIHNvZnR3YXJlIGFuZCBhc3NvY2lhdGVkIGRvY3VtZW50YXRpb24gZmlsZXMgKHRoZSAiU29mdHdhcmUiKSwgdG8gZGVhbAppbiB0aGUgU29mdHdhcmUgd2l0aG91dCByZXN0cmljdGlvbiwgaW5jbHVkaW5nIHdpdGhvdXQgbGltaXRhdGlvbiB0aGUgcmlnaHRzCnRvIHVzZSwgY29weSwgbW9kaWZ5LCBtZXJnZSwgcHVibGlzaCwgZGlzdHJpYnV0ZSwgc3VibGljZW5zZSwgYW5kL29yIHNlbGwKY29waWVzIG9mIHRoZSBTb2Z0d2FyZSwgYW5kIHRvIHBlcm1pdCBwZXJzb25zIHRvIHdob20gdGhlIFNvZnR3YXJlIGlzCmZ1cm5pc2hlZCB0byBkbyBzbywgc3ViamVjdCB0byB0aGUgZm9sbG93aW5nIGNvbmRpdGlvbnM6CgpUaGUgYWJvdmUgY29weXJpZ2h0IG5vdGljZSBhbmQgdGhpcyBwZXJtaXNzaW9uIG5vdGljZSBzaGFsbCBiZSBpbmNsdWRlZCBpbiBhbGwKY29waWVzIG9yIHN1YnN0YW50aWFsIHBvcnRpb25zIG9mIHRoZSBTb2Z0d2FyZS4KClRIRSBTT0ZUV0FSRSBJUyBQUk9WSURFRCAiQVMgSVMiLCBXSVRIT1VUIFdBUlJBTlRZIE9GIEFOWSBLSU5ELCBFWFBSRVNTIE9SCklNUExJRUQsIElOQ0xVRElORyBCVVQgTk9UIExJTUlURUQgVE8gVEhFIFdBUlJBTlRJRVMgT0YgTUVSQ0hBTlRBQklMSVRZLApGSVRORVNTIEZPUiBBIFBBUlRJQ1VMQVIgUFVSUE9TRSBBTkQgTk9OSU5GUklOR0VNRU5ULiBJTiBOTyBFVkVOVCBTSEFMTCBUSEUKQVVUSE9SUyBPUiBDT1BZUklHSFQgSE9MREVSUyBCRSBMSUFCTEUgRk9SIEFOWSBDTEFJTSwgREFNQUdFUyBPUiBPVEhFUgpMSUFCSUxJVFksIFdIRVRIRVIgSU4gQU4gQUNUSU9OIE9GIENPTlRSQUNULCBUT1JUIE9SIE9USEVSV0lTRSwgQVJJU0lORyBGUk9NLApPVVQgT0YgT1IgSU4gQ09OTkVDVElPTiBXSVRIIFRIRSBTT0ZUV0FSRSBPUiBUSEUgVVNFIE9SIE9USEVSIERFQUxJTkdTIElOIFRIRQpTT0ZUV0FSRS4KCk1hcCBpY29uIGZyb20gaHR0cHM6Ly9naXRodWIuY29tL2ZlYXRoZXJpY29ucy9mZWF0aGVyCgotLT4="

/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/

var stylesInDom = {};

var	memoize = function (fn) {
	var memo;

	return function () {
		if (typeof memo === "undefined") memo = fn.apply(this, arguments);
		return memo;
	};
};

var isOldIE = memoize(function () {
	// Test for IE <= 9 as proposed by Browserhacks
	// @see http://browserhacks.com/#hack-e71d8692f65334173fee715c222cb805
	// Tests for existence of standard globals is to allow style-loader
	// to operate correctly into non-standard environments
	// @see https://github.com/webpack-contrib/style-loader/issues/177
	return window && document && document.all && !window.atob;
});

var getTarget = function (target, parent) {
  if (parent){
    return parent.querySelector(target);
  }
  return document.querySelector(target);
};

var getElement = (function (fn) {
	var memo = {};

	return function(target, parent) {
                // If passing function in options, then use it for resolve "head" element.
                // Useful for Shadow Root style i.e
                // {
                //   insertInto: function () { return document.querySelector("#foo").shadowRoot }
                // }
                if (typeof target === 'function') {
                        return target();
                }
                if (typeof memo[target] === "undefined") {
			var styleTarget = getTarget.call(this, target, parent);
			// Special case to return head of iframe instead of iframe itself
			if (window.HTMLIFrameElement && styleTarget instanceof window.HTMLIFrameElement) {
				try {
					// This will throw an exception if access to iframe is blocked
					// due to cross-origin restrictions
					styleTarget = styleTarget.contentDocument.head;
				} catch(e) {
					styleTarget = null;
				}
			}
			memo[target] = styleTarget;
		}
		return memo[target]
	};
})();

var singleton = null;
var	singletonCounter = 0;
var	stylesInsertedAtTop = [];

var	fixUrls = __webpack_require__(7);

module.exports = function(list, options) {
	if (typeof DEBUG !== "undefined" && DEBUG) {
		if (typeof document !== "object") throw new Error("The style-loader cannot be used in a non-browser environment");
	}

	options = options || {};

	options.attrs = typeof options.attrs === "object" ? options.attrs : {};

	// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
	// tags it will allow on a page
	if (!options.singleton && typeof options.singleton !== "boolean") options.singleton = isOldIE();

	// By default, add <style> tags to the <head> element
        if (!options.insertInto) options.insertInto = "head";

	// By default, add <style> tags to the bottom of the target
	if (!options.insertAt) options.insertAt = "bottom";

	var styles = listToStyles(list, options);

	addStylesToDom(styles, options);

	return function update (newList) {
		var mayRemove = [];

		for (var i = 0; i < styles.length; i++) {
			var item = styles[i];
			var domStyle = stylesInDom[item.id];

			domStyle.refs--;
			mayRemove.push(domStyle);
		}

		if(newList) {
			var newStyles = listToStyles(newList, options);
			addStylesToDom(newStyles, options);
		}

		for (var i = 0; i < mayRemove.length; i++) {
			var domStyle = mayRemove[i];

			if(domStyle.refs === 0) {
				for (var j = 0; j < domStyle.parts.length; j++) domStyle.parts[j]();

				delete stylesInDom[domStyle.id];
			}
		}
	};
};

function addStylesToDom (styles, options) {
	for (var i = 0; i < styles.length; i++) {
		var item = styles[i];
		var domStyle = stylesInDom[item.id];

		if(domStyle) {
			domStyle.refs++;

			for(var j = 0; j < domStyle.parts.length; j++) {
				domStyle.parts[j](item.parts[j]);
			}

			for(; j < item.parts.length; j++) {
				domStyle.parts.push(addStyle(item.parts[j], options));
			}
		} else {
			var parts = [];

			for(var j = 0; j < item.parts.length; j++) {
				parts.push(addStyle(item.parts[j], options));
			}

			stylesInDom[item.id] = {id: item.id, refs: 1, parts: parts};
		}
	}
}

function listToStyles (list, options) {
	var styles = [];
	var newStyles = {};

	for (var i = 0; i < list.length; i++) {
		var item = list[i];
		var id = options.base ? item[0] + options.base : item[0];
		var css = item[1];
		var media = item[2];
		var sourceMap = item[3];
		var part = {css: css, media: media, sourceMap: sourceMap};

		if(!newStyles[id]) styles.push(newStyles[id] = {id: id, parts: [part]});
		else newStyles[id].parts.push(part);
	}

	return styles;
}

function insertStyleElement (options, style) {
	var target = getElement(options.insertInto)

	if (!target) {
		throw new Error("Couldn't find a style target. This probably means that the value for the 'insertInto' parameter is invalid.");
	}

	var lastStyleElementInsertedAtTop = stylesInsertedAtTop[stylesInsertedAtTop.length - 1];

	if (options.insertAt === "top") {
		if (!lastStyleElementInsertedAtTop) {
			target.insertBefore(style, target.firstChild);
		} else if (lastStyleElementInsertedAtTop.nextSibling) {
			target.insertBefore(style, lastStyleElementInsertedAtTop.nextSibling);
		} else {
			target.appendChild(style);
		}
		stylesInsertedAtTop.push(style);
	} else if (options.insertAt === "bottom") {
		target.appendChild(style);
	} else if (typeof options.insertAt === "object" && options.insertAt.before) {
		var nextSibling = getElement(options.insertAt.before, target);
		target.insertBefore(style, nextSibling);
	} else {
		throw new Error("[Style Loader]\n\n Invalid value for parameter 'insertAt' ('options.insertAt') found.\n Must be 'top', 'bottom', or Object.\n (https://github.com/webpack-contrib/style-loader#insertat)\n");
	}
}

function removeStyleElement (style) {
	if (style.parentNode === null) return false;
	style.parentNode.removeChild(style);

	var idx = stylesInsertedAtTop.indexOf(style);
	if(idx >= 0) {
		stylesInsertedAtTop.splice(idx, 1);
	}
}

function createStyleElement (options) {
	var style = document.createElement("style");

	if(options.attrs.type === undefined) {
		options.attrs.type = "text/css";
	}

	if(options.attrs.nonce === undefined) {
		var nonce = getNonce();
		if (nonce) {
			options.attrs.nonce = nonce;
		}
	}

	addAttrs(style, options.attrs);
	insertStyleElement(options, style);

	return style;
}

function createLinkElement (options) {
	var link = document.createElement("link");

	if(options.attrs.type === undefined) {
		options.attrs.type = "text/css";
	}
	options.attrs.rel = "stylesheet";

	addAttrs(link, options.attrs);
	insertStyleElement(options, link);

	return link;
}

function addAttrs (el, attrs) {
	Object.keys(attrs).forEach(function (key) {
		el.setAttribute(key, attrs[key]);
	});
}

function getNonce() {
	if (false) {}

	return __webpack_require__.nc;
}

function addStyle (obj, options) {
	var style, update, remove, result;

	// If a transform function was defined, run it on the css
	if (options.transform && obj.css) {
	    result = typeof options.transform === 'function'
		 ? options.transform(obj.css) 
		 : options.transform.default(obj.css);

	    if (result) {
	    	// If transform returns a value, use that instead of the original css.
	    	// This allows running runtime transformations on the css.
	    	obj.css = result;
	    } else {
	    	// If the transform function returns a falsy value, don't add this css.
	    	// This allows conditional loading of css
	    	return function() {
	    		// noop
	    	};
	    }
	}

	if (options.singleton) {
		var styleIndex = singletonCounter++;

		style = singleton || (singleton = createStyleElement(options));

		update = applyToSingletonTag.bind(null, style, styleIndex, false);
		remove = applyToSingletonTag.bind(null, style, styleIndex, true);

	} else if (
		obj.sourceMap &&
		typeof URL === "function" &&
		typeof URL.createObjectURL === "function" &&
		typeof URL.revokeObjectURL === "function" &&
		typeof Blob === "function" &&
		typeof btoa === "function"
	) {
		style = createLinkElement(options);
		update = updateLink.bind(null, style, options);
		remove = function () {
			removeStyleElement(style);

			if(style.href) URL.revokeObjectURL(style.href);
		};
	} else {
		style = createStyleElement(options);
		update = applyToTag.bind(null, style);
		remove = function () {
			removeStyleElement(style);
		};
	}

	update(obj);

	return function updateStyle (newObj) {
		if (newObj) {
			if (
				newObj.css === obj.css &&
				newObj.media === obj.media &&
				newObj.sourceMap === obj.sourceMap
			) {
				return;
			}

			update(obj = newObj);
		} else {
			remove();
		}
	};
}

var replaceText = (function () {
	var textStore = [];

	return function (index, replacement) {
		textStore[index] = replacement;

		return textStore.filter(Boolean).join('\n');
	};
})();

function applyToSingletonTag (style, index, remove, obj) {
	var css = remove ? "" : obj.css;

	if (style.styleSheet) {
		style.styleSheet.cssText = replaceText(index, css);
	} else {
		var cssNode = document.createTextNode(css);
		var childNodes = style.childNodes;

		if (childNodes[index]) style.removeChild(childNodes[index]);

		if (childNodes.length) {
			style.insertBefore(cssNode, childNodes[index]);
		} else {
			style.appendChild(cssNode);
		}
	}
}

function applyToTag (style, obj) {
	var css = obj.css;
	var media = obj.media;

	if(media) {
		style.setAttribute("media", media)
	}

	if(style.styleSheet) {
		style.styleSheet.cssText = css;
	} else {
		while(style.firstChild) {
			style.removeChild(style.firstChild);
		}

		style.appendChild(document.createTextNode(css));
	}
}

function updateLink (link, options, obj) {
	var css = obj.css;
	var sourceMap = obj.sourceMap;

	/*
		If convertToAbsoluteUrls isn't defined, but sourcemaps are enabled
		and there is no publicPath defined then lets turn convertToAbsoluteUrls
		on by default.  Otherwise default to the convertToAbsoluteUrls option
		directly
	*/
	var autoFixUrls = options.convertToAbsoluteUrls === undefined && sourceMap;

	if (options.convertToAbsoluteUrls || autoFixUrls) {
		css = fixUrls(css);
	}

	if (sourceMap) {
		// http://stackoverflow.com/a/26603875
		css += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + " */";
	}

	var blob = new Blob([css], { type: "text/css" });

	var oldSrc = link.href;

	link.href = URL.createObjectURL(blob);

	if(oldSrc) URL.revokeObjectURL(oldSrc);
}


/***/ }),
/* 7 */
/***/ (function(module, exports) {


/**
 * When source maps are enabled, `style-loader` uses a link element with a data-uri to
 * embed the css on the page. This breaks all relative urls because now they are relative to a
 * bundle instead of the current page.
 *
 * One solution is to only use full urls, but that may be impossible.
 *
 * Instead, this function "fixes" the relative urls to be absolute according to the current page location.
 *
 * A rudimentary test suite is located at `test/fixUrls.js` and can be run via the `npm test` command.
 *
 */

module.exports = function (css) {
  // get current location
  var location = typeof window !== "undefined" && window.location;

  if (!location) {
    throw new Error("fixUrls requires window.location");
  }

	// blank or null?
	if (!css || typeof css !== "string") {
	  return css;
  }

  var baseUrl = location.protocol + "//" + location.host;
  var currentDir = baseUrl + location.pathname.replace(/\/[^\/]*$/, "/");

	// convert each url(...)
	/*
	This regular expression is just a way to recursively match brackets within
	a string.

	 /url\s*\(  = Match on the word "url" with any whitespace after it and then a parens
	   (  = Start a capturing group
	     (?:  = Start a non-capturing group
	         [^)(]  = Match anything that isn't a parentheses
	         |  = OR
	         \(  = Match a start parentheses
	             (?:  = Start another non-capturing groups
	                 [^)(]+  = Match anything that isn't a parentheses
	                 |  = OR
	                 \(  = Match a start parentheses
	                     [^)(]*  = Match anything that isn't a parentheses
	                 \)  = Match a end parentheses
	             )  = End Group
              *\) = Match anything and then a close parens
          )  = Close non-capturing group
          *  = Match anything
       )  = Close capturing group
	 \)  = Match a close parens

	 /gi  = Get all matches, not the first.  Be case insensitive.
	 */
	var fixedCss = css.replace(/url\s*\(((?:[^)(]|\((?:[^)(]+|\([^)(]*\))*\))*)\)/gi, function(fullMatch, origUrl) {
		// strip quotes (if they exist)
		var unquotedOrigUrl = origUrl
			.trim()
			.replace(/^"(.*)"$/, function(o, $1){ return $1; })
			.replace(/^'(.*)'$/, function(o, $1){ return $1; });

		// already a full url? no change
		if (/^(#|data:|http:\/\/|https:\/\/|file:\/\/\/|\s*$)/i.test(unquotedOrigUrl)) {
		  return fullMatch;
		}

		// convert the url to a full url
		var newUrl;

		if (unquotedOrigUrl.indexOf("//") === 0) {
		  	//TODO: should we add protocol?
			newUrl = unquotedOrigUrl;
		} else if (unquotedOrigUrl.indexOf("/") === 0) {
			// path should be relative to the base url
			newUrl = baseUrl + unquotedOrigUrl; // already starts with '/'
		} else {
			// path should be relative to current directory
			newUrl = currentDir + unquotedOrigUrl.replace(/^\.\//, ""); // Strip leading './'
		}

		// send back the fixed url(...)
		return "url(" + JSON.stringify(newUrl) + ")";
	});

	// send back the fixed css
	return fixedCss;
};


/***/ }),
/* 8 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);

// CONCATENATED MODULE: ./src/app/shapes/shapes.js
var SHAPES = Object.freeze({
  RECTANGLE: "rect",
  CIRCLE: "circle",
  POLYGON: "polygon"
});
/* harmony default export */ var shapes_shapes = (SHAPES);
// CONCATENATED MODULE: ./src/app/shapes/point.js
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Point = function Point(x, y) {
  _classCallCheck(this, Point);

  this.x = x;
  this.y = y;
};

/* harmony default export */ var shapes_point = (Point);
// CONCATENATED MODULE: ./src/app/shapes/shapeClass.js
function shapeClass_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

/*eslint no-console: ["error", {"allow": ["warn"]}]*/



var shapeClass_Shape =
/*#__PURE__*/
function () {
  function Shape(context) {
    shapeClass_classCallCheck(this, Shape);

    this.id = Date.now();
    this.startPoint;
    this.endPoint;
    this.moveOffSet;
    this.dimensions = {
      frame: {},
      shape: {}
    };
    this.context = context;
    this.drawing = true;
    this.moving = false;
    this.selected = false;
    this.resizing = false;
    this.path = new Path2D();
    this.strokeStyle = "#2f4f4f";
    this.href = "";
  } //ABSTRACT METHODS


  _createClass(Shape, [{
    key: "draw",
    value: function draw() {
      console.warn("Implement method to render the shape");
    }
  }, {
    key: "getArea",
    value: function getArea() {
      console.warn("Implement method to return the shapes area in pixels");
    }
  }, {
    key: "getAreaCoords",
    value: function getAreaCoords() {
      console.warn("Implement method to return the coordinates of the shape");
    }
  }, {
    key: "updateDimensions",
    value: function updateDimensions() {
      console.warn("Implement method to update the shape's dimensions/points");
    }
  }, {
    key: "updateCoordinates",
    value: function updateCoordinates() {
      console.warn("Implement method to update the shape's location on canvas");
    }
  }, {
    key: "calcDrawCoords",
    value: function calcDrawCoords() {
      console.warn("Implement method to update shape's draw coordinates");
    } //CLASS METHODS

  }, {
    key: "contains",
    value: function contains(point) {
      return this.context.isPointInPath(this.path, point.x, point.y);
    }
  }, {
    key: "setMoveOffset",
    value: function setMoveOffset(newPoint) {
      var _x$y = {
        x: newPoint.x - this.startPoint.x,
        y: newPoint.y - this.startPoint.y
      },
          x = _x$y.x,
          y = _x$y.y;
      this.moveOffSet = new shapes_point(x, y);
      return this;
    }
  }, {
    key: "updateStartPoint",
    value: function updateStartPoint(newPoint) {
      var _x$y2 = {
        x: newPoint.x - this.moveOffSet.x,
        y: newPoint.y - this.moveOffSet.y
      },
          x = _x$y2.x,
          y = _x$y2.y;
      this.startPoint = new shapes_point(x, y);
      return this;
    }
  }, {
    key: "updateEndPoint",
    value: function updateEndPoint(newPoint) {
      this.endPoint = newPoint;
      return this;
    }
  }, {
    key: "drag",
    value: function drag() {
      this.moving = true;
      return this;
    }
  }, {
    key: "drop",
    value: function drop() {
      this.moving = false;
      return this;
    }
  }, {
    key: "select",
    value: function select() {
      this.selected = true;
      this.strokeStyle = "#C80000";
      return this;
    }
  }, {
    key: "deselect",
    value: function deselect() {
      this.selected = false;
      this.strokeStyle = "#2f4f4f";
      return this;
    }
  }, {
    key: "finishDrawing",
    value: function finishDrawing() {
      this.drawing = false;
      return this;
    }
  }, {
    key: "setDrawCoords",
    value: function setDrawCoords(coords) {
      this.startPoint = coords.start;
      this.endPoint = coords.end;
      this.moveOffSet = coords.start;
      return this;
    }
  }, {
    key: "setShapeDimensions",
    value: function setShapeDimensions(dimensions) {
      this.dimensions.shape = dimensions;
      return this;
    }
  }, {
    key: "setHref",
    value: function setHref(href) {
      this.href = href;
      return this;
    }
  }, {
    key: "addTransparentFill",
    value: function addTransparentFill(path) {
      this.context.globalAlpha = 0.3;
      this.context.fillStyle = "#c3c3c3";
      this.context.fill(path);
      this.context.globalAlpha = 1;
    }
  }, {
    key: "inUse",
    value: function inUse() {
      return this.drawing || this.moving || this.resizing;
    }
  }, {
    key: "toMapArea",
    value: function toMapArea() {
      var _this = this;

      var href = function href() {
        if (!/https?:\/\//.test(_this.href)) {
          return "http://" + _this.href;
        }

        return _this.href;
      };

      return new mapArea(this.type, this.getAreaCoords(), href());
    }
  }]);

  return Shape;
}();

/* harmony default export */ var shapeClass = (shapeClass_Shape);
// CONCATENATED MODULE: ./src/app/shapes/circle.js
function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function circle_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function circle_defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function circle_createClass(Constructor, protoProps, staticProps) { if (protoProps) circle_defineProperties(Constructor.prototype, protoProps); if (staticProps) circle_defineProperties(Constructor, staticProps); return Constructor; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }



var Circle =
/*#__PURE__*/
function (_Shape) {
  _inherits(Circle, _Shape);

  function Circle(context) {
    var _this;

    circle_classCallCheck(this, Circle);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(Circle).call(this, context));
    _this.type = "circle";
    return _this;
  }

  circle_createClass(Circle, [{
    key: "draw",
    value: function draw() {
      if (this.drawing) {
        this.context.strokeStyle = "#E2E2E2";
        this.context.strokeRect(this.dimensions.frame.x, this.dimensions.frame.y, this.dimensions.frame.w, this.dimensions.frame.h);
      }

      this.path = new Path2D();
      this.path.arc(this.dimensions.shape.cx, this.dimensions.shape.cy, Math.abs(this.dimensions.shape.r), 0, 2 * Math.PI, false);
      this.path.closePath();
      this.context.strokeStyle = this.strokeStyle;
      this.context.stroke(this.path);
      this.addTransparentFill(this.path);
    }
  }, {
    key: "getArea",
    value: function getArea() {
      var _this$getAreaCoords = this.getAreaCoords(),
          r = _this$getAreaCoords.r;

      return Math.round(Math.PI * Math.pow(r, 2));
    }
  }, {
    key: "getAreaCoords",
    value: function getAreaCoords() {
      return {
        cx: this.dimensions.shape.cx,
        cy: this.dimensions.shape.cy,
        r: this.dimensions.shape.r
      };
    }
  }, {
    key: "updateDimensions",
    value: function updateDimensions() {
      this.calculateBoundingBox();
      var circle = {
        cx: Math.round(this.dimensions.frame.x + this.dimensions.frame.w / 2),
        cy: Math.round(this.dimensions.frame.y + this.dimensions.frame.h / 2),
        r: Math.round(Math.abs(this.dimensions.frame.w / 2))
      };
      this.dimensions.shape = circle;
      return this;
    }
  }, {
    key: "updateCoordinates",
    value: function updateCoordinates() {
      this.dimensions.frame = Object.assign(this.dimensions.frame, {
        x: this.startPoint.x,
        y: this.startPoint.y
      });
      this.dimensions.shape = Object.assign(this.dimensions.shape, {
        cx: Math.round(this.dimensions.frame.x + this.dimensions.frame.w / 2),
        cy: Math.round(this.dimensions.frame.y + this.dimensions.frame.h / 2),
        r: Math.round(Math.abs(this.dimensions.frame.w / 2))
      });
      return this;
    }
  }, {
    key: "calcDrawCoords",
    value: function calcDrawCoords() {
      this.startPoint = {
        x: Math.round(this.dimensions.shape.cx - this.dimensions.shape.r),
        y: Math.round(this.dimensions.shape.cy - this.dimensions.shape.r)
      };
      this.endPoint = {
        x: Math.round(this.dimensions.shape.cx + this.dimensions.shape.r),
        y: Math.round(this.dimensions.shape.cy + this.dimensions.shape.r)
      };
      this.moveOffSet = Object.assign({}, this.startPoint);
      this.calculateBoundingBox();
      return this;
    }
  }, {
    key: "calculateBoundingBox",
    value: function calculateBoundingBox() {
      //Square frame that contains the circle
      var width = 0;
      var height = 0;
      var lengthX = Math.abs(this.endPoint.x - this.startPoint.x);
      var lengthY = Math.abs(this.endPoint.y - this.startPoint.y);

      if (lengthX < lengthY) {
        width = lengthX * (this.endPoint.x < this.startPoint.x ? -1 : 1);
        height = lengthX * (this.endPoint.y < this.startPoint.y ? -1 : 1);
      } else {
        width = lengthY * (this.endPoint.x < this.startPoint.x ? -1 : 1);
        height = lengthY * (this.endPoint.y < this.startPoint.y ? -1 : 1);
      }

      this.dimensions.frame = {
        x: this.startPoint.x,
        y: this.startPoint.y,
        w: Math.round(width),
        h: Math.round(height)
      };
      return this;
    }
  }]);

  return Circle;
}(shapeClass);

/* harmony default export */ var circle = (Circle);
// CONCATENATED MODULE: ./src/app/shapes/rectangle.js
function rectangle_typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { rectangle_typeof = function _typeof(obj) { return typeof obj; }; } else { rectangle_typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return rectangle_typeof(obj); }

function rectangle_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function rectangle_defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function rectangle_createClass(Constructor, protoProps, staticProps) { if (protoProps) rectangle_defineProperties(Constructor.prototype, protoProps); if (staticProps) rectangle_defineProperties(Constructor, staticProps); return Constructor; }

function rectangle_possibleConstructorReturn(self, call) { if (call && (rectangle_typeof(call) === "object" || typeof call === "function")) { return call; } return rectangle_assertThisInitialized(self); }

function rectangle_assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function rectangle_getPrototypeOf(o) { rectangle_getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return rectangle_getPrototypeOf(o); }

function rectangle_inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) rectangle_setPrototypeOf(subClass, superClass); }

function rectangle_setPrototypeOf(o, p) { rectangle_setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return rectangle_setPrototypeOf(o, p); }



var Rectangle =
/*#__PURE__*/
function (_Shape) {
  rectangle_inherits(Rectangle, _Shape);

  function Rectangle(context) {
    var _this;

    rectangle_classCallCheck(this, Rectangle);

    _this = rectangle_possibleConstructorReturn(this, rectangle_getPrototypeOf(Rectangle).call(this, context));
    _this.type = "rect";
    return _this;
  }

  rectangle_createClass(Rectangle, [{
    key: "draw",
    value: function draw() {
      this.path = new Path2D();
      this.path.rect(this.dimensions.shape.x, this.dimensions.shape.y, this.dimensions.shape.w, this.dimensions.shape.h);
      this.context.strokeStyle = this.strokeStyle;
      this.context.stroke(this.path);
      this.addTransparentFill(this.path);
    }
  }, {
    key: "getArea",
    value: function getArea() {
      return this.dimensions.shape.w * this.dimensions.shape.h;
    }
  }, {
    key: "getAreaCoords",
    value: function getAreaCoords() {
      return this.dimensions.shape;
    }
  }, {
    key: "updateDimensions",
    value: function updateDimensions() {
      var width = this.endPoint.x - this.startPoint.x;
      var height = this.endPoint.y - this.startPoint.y;
      this.dimensions.shape = {
        x: this.startPoint.x,
        y: this.startPoint.y,
        w: Math.round(width),
        h: Math.round(height)
      };
      this.dimensions.frame = Object.assign({}, this.dimensions.shape);
      return this;
    }
  }, {
    key: "updateCoordinates",
    value: function updateCoordinates() {
      this.dimensions.shape = Object.assign(this.dimensions.shape, {
        x: this.startPoint.x,
        y: this.startPoint.y
      });
      this.dimensions.frame = Object.assign({}, this.dimensions.shape);
      return this;
    }
  }, {
    key: "calcDrawCoords",
    value: function calcDrawCoords() {
      this.startPoint = {
        x: this.dimensions.shape.x,
        y: this.dimensions.shape.y
      };
      this.endPoint = {
        x: this.dimensions.shape.x + this.dimensions.shape.w,
        y: this.dimensions.shape.y + this.dimensions.shape.h
      };
      this.moveOffSet = Object.assign({}, this.startPoint);
      return this;
    }
  }]);

  return Rectangle;
}(shapeClass);

/* harmony default export */ var rectangle = (Rectangle);
// CONCATENATED MODULE: ./src/app/shapes/polygon.js
function polygon_typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { polygon_typeof = function _typeof(obj) { return typeof obj; }; } else { polygon_typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return polygon_typeof(obj); }

function polygon_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function polygon_defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function polygon_createClass(Constructor, protoProps, staticProps) { if (protoProps) polygon_defineProperties(Constructor.prototype, protoProps); if (staticProps) polygon_defineProperties(Constructor, staticProps); return Constructor; }

function polygon_possibleConstructorReturn(self, call) { if (call && (polygon_typeof(call) === "object" || typeof call === "function")) { return call; } return polygon_assertThisInitialized(self); }

function polygon_assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function polygon_getPrototypeOf(o) { polygon_getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return polygon_getPrototypeOf(o); }

function polygon_inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) polygon_setPrototypeOf(subClass, superClass); }

function polygon_setPrototypeOf(o, p) { polygon_setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return polygon_setPrototypeOf(o, p); }



var Polygon =
/*#__PURE__*/
function (_Shape) {
  polygon_inherits(Polygon, _Shape);

  function Polygon(context) {
    var _this;

    polygon_classCallCheck(this, Polygon);

    _this = polygon_possibleConstructorReturn(this, polygon_getPrototypeOf(Polygon).call(this, context));
    _this.type = "polygon";
    _this.dimensions.shape = [];
    return _this;
  }

  polygon_createClass(Polygon, [{
    key: "draw",
    value: function draw() {
      var _this2 = this;

      this.path = new Path2D();
      this.path.moveTo(this.dimensions.shape[0].x, this.dimensions.shape[0].y);
      this.dimensions.shape.forEach(function (point) {
        _this2.path.lineTo(point.x, point.y);
      });

      if (this.drawing) {
        this.path.lineTo(this.endPoint.x, this.endPoint.y);
      }

      this.path.closePath();
      this.context.strokeStyle = this.strokeStyle;
      this.context.stroke(this.path);
      this.addTransparentFill(this.path);
    }
  }, {
    key: "getArea",
    value: function getArea() {
      var points = this.dimensions.shape;
      var n = points.length;
      var area = 0;
      var prev = n - 1;

      if (n > 2) {
        for (var curr = 0; curr < n; curr++) {
          area += points[prev].x * points[curr].y;
          area -= points[curr].x * points[prev].y;
          prev = curr;
        }
      }

      return Math.round(Math.abs(area) / 2);
    }
  }, {
    key: "getAreaCoords",
    value: function getAreaCoords() {
      return this.dimensions.shape;
    }
  }, {
    key: "updateDimensions",
    value: function updateDimensions(block) {
      var _this3 = this;

      if (block) {
        return;
      }

      var exists = this.dimensions.shape.find(function (pair) {
        return pair.x === _this3.endPoint.x && pair.y === _this3.endPoint.y;
      });

      if (!exists) {
        this.dimensions.shape.push({
          x: Math.round(this.endPoint.x),
          y: Math.round(this.endPoint.y)
        });
      }

      this.dimensions.shape = this.dimensions.shape;
      return this;
    }
  }, {
    key: "updateCoordinates",
    value: function updateCoordinates() {
      var delta = {
        x: this.startPoint.x - Math.abs(this.dimensions.shape[0].x),
        y: this.startPoint.y - Math.abs(this.dimensions.shape[0].y)
      };
      var newCoords = this.dimensions.shape.map(function (pair) {
        return {
          x: pair.x + delta.x,
          y: pair.y + delta.y
        };
      });
      this.dimensions.shape = newCoords;
      return this;
    }
  }, {
    key: "calcDrawCoords",
    value: function calcDrawCoords() {
      this.startPoint = this.dimensions.shape[0];
      this.endPoint = Object.assign({}, this.startPoint);
      this.moveOffSet = Object.assign({}, this.startPoint);
      return this;
    }
  }]);

  return Polygon;
}(shapeClass);

/* harmony default export */ var polygon = (Polygon);
// CONCATENATED MODULE: ./src/app/shapes/shapeFactory.js
var shapeFactory_this = undefined;






var shapeFactory_ShapeFactory = function ShapeFactory(shape, options) {
  var context = options.context,
      drawCoords = options.drawCoords,
      areaCoords = options.areaCoords,
      href = options.href;
  var shapeObj;

  switch (shape) {
    case shapes_shapes.CIRCLE:
      shapeObj = new circle(context);
      break;

    case shapes_shapes.RECTANGLE:
      shapeObj = new rectangle(context);
      break;

    case shapes_shapes.POLYGON:
      shapeObj = new polygon(context);
      break;

    default:
      throw new Error("Invalid shape passed to constructor: " + shapeFactory_this.shape);
  }

  if (drawCoords) {
    shapeObj.setDrawCoords(drawCoords);
  }

  if (areaCoords) {
    shapeObj.setShapeDimensions(areaCoords).calcDrawCoords().finishDrawing();
  }

  if (href) {
    shapeObj.setHref(href);
  }

  return shapeObj;
};

/* harmony default export */ var shapeFactory = (shapeFactory_ShapeFactory);
// CONCATENATED MODULE: ./src/app/utils/mapArea.js
function mapArea_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function mapArea_defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function mapArea_createClass(Constructor, protoProps, staticProps) { if (protoProps) mapArea_defineProperties(Constructor.prototype, protoProps); if (staticProps) mapArea_defineProperties(Constructor, staticProps); return Constructor; }

/*eslint no-console: ["error", {"allow": ["error"]}]*/



var mapArea_MapArea =
/*#__PURE__*/
function () {
  function MapArea(shape, coords, href) {
    mapArea_classCallCheck(this, MapArea);

    this.shape = shape;
    this.coords = typeof coords === "string" ? coords : this.formatCoordsToString(coords);
    this.href = href;
    this.target = "_blank";
  }

  mapArea_createClass(MapArea, [{
    key: "formatCoordsToString",
    value: function formatCoordsToString(coords) {
      switch (this.shape) {
        case shapes_shapes.CIRCLE:
          return "".concat(coords.cx, ",").concat(coords.cy, ",").concat(coords.r);

        case shapes_shapes.RECTANGLE:
          return "".concat(coords.x, ",").concat(coords.y, ",").concat(coords.x + coords.w, ",").concat(coords.y + coords.h);

        case shapes_shapes.POLYGON:
          return coords.map(function (pair) {
            return "".concat(pair.x, ",").concat(pair.y);
          }).join(",");

        default:
          throw new Error("Invalid shape passed to constructor");
      }
    }
  }, {
    key: "formatCoordsToObjectDimensions",
    value: function formatCoordsToObjectDimensions(coords) {
      var temp = coords.split(",").map(function (item) {
        return Number(item);
      });

      switch (this.shape) {
        case shapes_shapes.CIRCLE:
          return {
            cx: Number(temp[0]),
            cy: Number(temp[1]),
            r: Number(temp[2])
          };

        case shapes_shapes.RECTANGLE:
          return {
            x: temp[0],
            y: temp[1],
            w: Math.round(temp[2] - temp[0]),
            h: Math.round(temp[3] - temp[1])
          };

        case shapes_shapes.POLYGON:
          {
            var points = [];

            for (var i = 0; i < temp.length; i += 2) {
              var point = {
                x: temp[i],
                y: temp[i + 1]
              };
              points.push(point);
            }

            return points;
          }
      }
    }
  }, {
    key: "toShape",
    value: function toShape(context) {
      var coords = this.formatCoordsToObjectDimensions(this.coords);
      return new shapeFactory(this.shape, {
        areaCoords: coords,
        context: context,
        href: this.href
      });
    }
  }]);

  return MapArea;
}();

/* harmony default export */ var mapArea = (mapArea_MapArea);
// CONCATENATED MODULE: ./src/app/utils/mapHelper.js


var clearAreas = function clearAreas(map) {
  if (map) {
    var children = Array.from(map.children);

    for (var i = 0; i < children.length; i++) {
      map.removeChild(children[i]);
    }
  }
};

var generateUniqueMapName = function generateUniqueMapName(maps) {
  var template = "tinymce-map";

  var condition = function condition() {
    return maps.find(function (map) {
      return map.name === name;
    });
  };

  var name = template;
  var counter = 1;

  while (condition()) {
    name = template + counter;
    counter++;
  }

  return name;
};

var addMapElement = function addMapElement(editor, img) {
  var parent = img.parentNode;
  var name = generateUniqueMapName(Array.from(editor.dom.select("map")));
  editor.dom.add(parent, "map", {
    id: name,
    name: name
  });
  img.useMap = "#" + name;
  return editor.dom.select("map")[0];
};

var mapHelper = {
  load: function load(elements) {
    return elements.map(function (el) {
      return new mapArea(el.shape, el.coords, el.href);
    });
  },
  write: function write(editor, img) {
    var app = document.tinymceImageMap;
    var map = editor.dom.select("map").find(function (item) {
      return "#" + item.name === img.useMap;
    }) || addMapElement(editor, img);
    clearAreas(map);
    app.shapes.forEach(function (shape) {
      var area = shape.toMapArea();
      var node = Object.assign(editor.dom.create("area"), area);
      node.style.cursor = "text";
      map.append(node);
    });
  },
  addMapElement: addMapElement
};
/* harmony default export */ var utils_mapHelper = (mapHelper);
// CONCATENATED MODULE: ./src/app/actions/draw.js


var draw = {
  onMouseDown: function onMouseDown(scope, coords, e) {
    var points = {
      start: coords,
      end: coords
    };

    if (!scope.shapeInProgress) {
      scope.shapeInProgress = shapeFactory(scope.currentDrawShape, {
        context: scope.context,
        drawCoords: points
      });
      scope.shapeInProgress.updateDimensions();
      scope.shapes.push(scope.shapeInProgress);
    } else if (e.shiftKey) {
      scope.shapeInProgress.updateEndPoint(coords).updateDimensions();
      finishDrawing.call(scope);
    } else {
      scope.shapeInProgress.updateEndPoint(coords).updateDimensions();
    }
  },
  onMouseMove: function onMouseMove(scope, coords) {
    if (scope.shapeInProgress) {
      scope.shapeInProgress.type === shapes_shapes.POLYGON ? scope.shapeInProgress.updateEndPoint(coords).updateDimensions(true) : scope.shapeInProgress.updateEndPoint(coords).updateDimensions();
    }
  },
  onMouseUp: function onMouseUp(scope) {
    if (scope.shapeInProgress && scope.shapeInProgress.type !== shapes_shapes.POLYGON) {
      finishDrawing.call(scope);
    }
  }
};

function finishDrawing() {
  this.shapeInProgress.finishDrawing();
  this.shapes = this.shapes.filter(removeInvalidShape);
  this.setFocusedShape(this.shapeInProgress);
  this.shapeInProgress = undefined;
}

function removeInvalidShape(shape) {
  return shape.getArea() > 300;
}

/* harmony default export */ var actions_draw = (draw);
// CONCATENATED MODULE: ./src/app/actions/move.js
var move = {
  onMouseDown: function onMouseDown(scope, coords) {
    scope.focusedShape.drag().setMoveOffset(coords);
  },
  onMouseMove: function onMouseMove(scope, coords) {
    scope.focusedShape.updateStartPoint(coords).updateCoordinates();
  },
  onMouseUp: function onMouseUp(scope) {
    scope.focusedShape.drop();
  }
};
/* harmony default export */ var actions_move = (move);
// CONCATENATED MODULE: ./src/app/actions/resize.js
/* eslint-disable no-unused-vars */
//TODO: Implement resizing shapes. Ref: https://github.com/tiberius-s/tinymce-image-map/issues/1

var resize = {
  onMouseDown: function onMouseDown(scope, coords, e) {},
  onMouseMove: function onMouseMove(scope, coords, e) {},
  onMouseUp: function onMouseUp(scope, coords, e) {}
};
/* harmony default export */ var actions_resize = (resize);
// CONCATENATED MODULE: ./src/app/actions/actionDispatcher.js



var ActionDispatcher = {
  draw: actions_draw,
  move: actions_move,
  resize: actions_resize
};
/* harmony default export */ var actionDispatcher = (ActionDispatcher);
// CONCATENATED MODULE: ./src/app/actions/actions.js
var ACTIONS = Object.freeze({
  DRAW: "draw",
  MOVE: "move",
  RESIZE: "resize"
});
/* harmony default export */ var actions = (ACTIONS);
// CONCATENATED MODULE: ./src/app/main.js
function _toConsumableArray(arr) { return _arrayWithoutHoles(arr) || _iterableToArray(arr) || _nonIterableSpread(); }

function _nonIterableSpread() { throw new TypeError("Invalid attempt to spread non-iterable instance"); }

function _iterableToArray(iter) { if (Symbol.iterator in Object(iter) || Object.prototype.toString.call(iter) === "[object Arguments]") return Array.from(iter); }

function _arrayWithoutHoles(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = new Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } }

function main_classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function main_defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function main_createClass(Constructor, protoProps, staticProps) { if (protoProps) main_defineProperties(Constructor.prototype, protoProps); if (staticProps) main_defineProperties(Constructor, staticProps); return Constructor; }






var main_App =
/*#__PURE__*/
function () {
  function App(args) {
    var _this = this;

    main_classCallCheck(this, App);

    this.args = args;
    this.canvas = args.canvas;
    this.context = this.canvas.getContext("2d");
    this.shapeSelectors = args.shapeSelectors;
    this.currentDrawShape;
    this.currentAction;
    this.shapeInProgress;
    this.focusedShape;
    this.shapes = [];
    this.isMouseDown = false;
    this.img;
    this.urlInput = args.urlInput;
    this.hint = args.hint;
    this.canvas.addEventListener("mousedown", function (e) {
      return _this.mouseDown(e);
    }, false);
    this.canvas.addEventListener("mousemove", function (e) {
      return _this.mouseMove(e);
    }, false);
    this.canvas.addEventListener("mouseup", function (e) {
      return _this.mouseUp(e);
    }, false);
    this.urlInput.addEventListener("change", function (e) {
      if (_this.focusedShape) {
        _this.focusedShape.setHref(e.target.value);
      }
    });
    this.shapeSelectors.forEach(function (node) {
      return node.addEventListener("change", function () {
        return _this.setShape();
      }, false);
    });
  }

  main_createClass(App, [{
    key: "render",
    value: function render() {
      if (this.canvas.getContext) {
        this.clearCanvas();
        this.drawImage();
        this.shapes.forEach(function (r) {
          return r.draw();
        });
        this.showHint();
      }
    }
  }, {
    key: "mouseDown",
    value: function mouseDown(e) {
      if (e.button === 0) {
        this.isMouseDown = true;
        var coords = this.getMouseCoords(e);
        this.determineAction(coords);
        actionDispatcher[this.currentAction].onMouseDown(this, coords, e);
        this.render();
      }

      e.preventDefault();
    }
  }, {
    key: "mouseMove",
    value: function mouseMove(e) {
      if (this.isMouseDown || this.shapeInProgress && this.shapeInProgress.inUse()) {
        var coords = this.getMouseCoords(e);
        actionDispatcher[this.currentAction].onMouseMove(this, coords, e);
        this.render();
      }

      e.preventDefault();
    }
  }, {
    key: "mouseUp",
    value: function mouseUp(e) {
      if (this.isMouseDown) {
        this.isMouseDown = false;
        var coords = this.getMouseCoords(e);
        actionDispatcher[this.currentAction].onMouseUp(this, coords, e);
        this.render();
      }

      e.preventDefault();
    }
  }, {
    key: "getMouseCoords",
    value: function getMouseCoords(e) {
      var boundingRect = this.canvas.getBoundingClientRect();
      var x = Math.round(e.clientX - boundingRect.left);
      var y = Math.round(e.clientY - boundingRect.top);
      return new shapes_point(x, y);
    }
  }, {
    key: "clearCanvas",
    value: function clearCanvas(del) {
      if (del) {
        this.shapeInProgress = undefined;
        this.focusedShape = undefined;
        this.shapes = [];
      }

      this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
      this.drawImage();
    }
  }, {
    key: "setShape",
    value: function setShape() {
      var shapes = _toConsumableArray(this.shapeSelectors);

      var selected = shapes.find(function (shape) {
        return shape.checked;
      });
      selected !== undefined ? this.currentDrawShape = shapes_shapes[selected.value.toUpperCase()] : this.currentDrawShape = shapes_shapes.CIRCLE;

      if (!selected) {
        shapes.find(function (shape) {
          return shape.value === shapes_shapes.CIRCLE;
        }).checked = true;
      }
    }
  }, {
    key: "determineAction",
    value: function determineAction(coords) {
      var shape = this.shapes.find(function (shape) {
        return shape.contains(coords);
      });

      if (shape) {
        if (shape.drawing) {
          return;
        }

        this.setFocusedShape(shape);
        this.currentAction = actions.MOVE;
      } else {
        this.setFocusedShape();
        this.currentAction = actions.DRAW;
      }
    }
  }, {
    key: "setFocusedShape",
    value: function setFocusedShape(shape) {
      if (shape) {
        this.removeFocusedShape();
        this.focusedShape = shape;
        this.focusedShape.select();
        this.urlInput.value = this.focusedShape.href;
      } else {
        this.removeFocusedShape();
        this.urlInput.value = "";
      }
    }
  }, {
    key: "removeFocusedShape",
    value: function removeFocusedShape() {
      if (this.focusedShape) {
        this.urlInput.blur();
        this.focusedShape.deselect();
      }
    }
  }, {
    key: "deleteMap",
    value: function deleteMap() {
      if (this.focusedShape) {
        var index = this.shapes.findIndex(function (shape) {
          return shape.selected;
        });
        this.shapes.splice(index, 1);
        this.focusedShape = undefined;
        this.render();
      }
    }
  }, {
    key: "loadImage",
    value: function loadImage() {
      var _this2 = this;

      this.img = new Image();

      this.img.onload = function () {
        _this2.drawImage();

        _this2.shapes.forEach(function (r) {
          return r.draw();
        });
      };

      this.img.height = this.args.img.height;
      this.img.width = this.args.img.width;
      this.img.src = this.args.img.src;
    }
  }, {
    key: "drawImage",
    value: function drawImage() {
      var _this$context;

      var destSize = [0, 0, this.canvas.width, this.canvas.height];
      this.context.imageSmoothingEnabled = false;

      (_this$context = this.context).drawImage.apply(_this$context, [this.img].concat(destSize));
    }
  }, {
    key: "showHint",
    value: function showHint() {
      if (this.shapeInProgress && this.shapeInProgress.type === "polygon" && this.shapeInProgress.drawing) {
        this.hint.style.visibility = "visible";
      } else {
        this.hint.style.visibility = "hidden";
      }
    }
  }, {
    key: "init",
    value: function init() {
      var _this3 = this;

      this.setShape();
      this.loadImage();
      this.args.map.areas.forEach(function (area) {
        var shape = area.toShape(_this3.context);

        _this3.shapes.push(shape);
      });
    }
  }]);

  return App;
}();

/* harmony default export */ var main = (main_App);
// EXTERNAL MODULE: ./src/app/utils/template.html
var template = __webpack_require__(0);
var template_default = /*#__PURE__*/__webpack_require__.n(template);

// EXTERNAL MODULE: ./src/app/utils/styles.css
var styles = __webpack_require__(1);

// CONCATENATED MODULE: ./src/app/utils/view.js




var view = {
  createDialogHtml: function createDialogHtml(editor) {
    return new Promise(function (resolve) {
      var doc = getImageMapDocument(editor);
      var container = doc.$("#img-map-container")[0];
      container.innerHTML = template_default.a;
      resolve(doc);
    });
  },
  initApp: function initApp(editor, doc, img) {
    var map = editor.dom.select("map").find(function (item) {
      return "#" + item.name === img.useMap;
    });

    if (!map) {
      img.useMap = "";
    }

    var areas = img.useMap === "" ? [] : utils_mapHelper.load(Array.from(map.children));
    var canvas = doc.byId("img-map-canvas");
    canvas.setAttribute("height", img.height);
    canvas.setAttribute("width", img.width);
    var urlInput = doc.byId("map-url-input");
    var shapeSelectors = doc.byName("shapeSelect");
    var hint = doc.byId("img-map-hint");
    var args = {
      canvas: canvas,
      hint: hint,
      img: {
        src: img.src,
        height: img.height,
        width: img.width,
        useMap: img.useMap
      },
      map: {
        name: img.useMap,
        areas: areas
      },
      shapeSelectors: shapeSelectors,
      urlInput: urlInput
    };
    var app = new main(args);
    document.tinymceImageMap = app;
    app.init();
  },
  destroy: function destroy(editor, img) {
    var doc = getImageMapDocument(editor);
    doc.byId("map-url-input").blur();
    utils_mapHelper.write(editor, img);
    document.tinymceImageMap = {};
    doc.byId("img-map-container").innerHTML = "";
  }
};

function getImageMapDocument(editor) {
  return editor.windowManager.getWindows().filter(function (win) {
    return Array.from(win.$("#img-map-container")).length === 1;
  }).map(function (win) {
    win.byId = function (id) {
      return this.$("#".concat(id))[0];
    };

    win.byName = function (name) {
      return Array.from(this.$("[name=".concat(name, "]")));
    };

    return win;
  }).pop();
}

/* harmony default export */ var utils_view = (view);
// CONCATENATED MODULE: ./src/plugin.js


var plugin_openDialog = function openDialog(editor) {
  var isImg = editor.selection.getNode().nodeName === "IMG";

  if (isImg) {
    var img = editor.selection.getNode();
    editor.windowManager.open({
      title: "Manage Image Maps",
      body: [{
        type: "container",
        layout: "flex",
        direction: "column",
        align: "center",
        html: '<div id="img-map-container"></div>',
        minHeight: img.height < 800 ? img.height + 100 : 900,
        minWidth: img.width > 450 ? img.width : 450,
        autoScroll: true
      }],
      buttons: [{
        text: "Done",
        onclick: "submit"
      }],
      onsubmit: function onsubmit() {
        utils_view.destroy(editor, img);
      }
    });
    utils_view.createDialogHtml(editor).then(function (doc) {
      return utils_view.initApp(editor, doc, img);
    });
  }
};

var plugin_plugin = function plugin(editor) {
  editor.on("init", function () {
    editor.dom.select("area").forEach(function (area) {
      return area.style.cursor = "text";
    });
  });
  editor.on("click", function (e) {
    if (e.target.nodeName === "AREA") {
      var map = e.target.parentNode;
      editor.selection.select(editor.dom.select("img").find(function (img) {
        return img.useMap === "#" + map.name;
      }));
    }
  });
  editor.addMenuItem("imageMap", {
    icon: "img-map-icon",
    text: "Image Map",
    onclick: function onclick() {
      return plugin_openDialog(editor);
    }
  });
  editor.addButton("imageMap", {
    tooltip: "Image Map",
    icon: "img-map-icon",
    onclick: function onclick() {
      return plugin_openDialog(editor);
    }
  });
};

/* harmony default export */ var src_plugin = (plugin_plugin);
// CONCATENATED MODULE: ./src/index.js

tinymce.PluginManager.add("imageMap", src_plugin);
/* harmony default export */ var src = __webpack_exports__["default"] = (src_plugin);

/***/ })
/******/ ]);