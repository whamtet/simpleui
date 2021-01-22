var requestConfig;
var toSwap;

htmx.defineExtension('static', {
  onEvent : function(name, evt) {
    if (name === 'htmx:beforeRequest') {
      requestConfig = evt.detail.requestConfig;
    }
    if (name === 'htmx:beforeSwap') {
      toSwap = eval(evt.detail.xhr.response + '_static')(requestConfig);
      if (toSwap === undefined) {
        evt.preventDefault();
      }
    }
  },
  transformResponse: function(text, xhr, elt) {
    return toSwap;
  }
});
