var requestConfig;
var toSwap;

document.body.addEventListener('htmx:beforeRequest', function(info) {
  requestConfig = info.detail.requestConfig;
});

document.body.addEventListener('htmx:beforeSwap', function(info) {
  toSwap = eval(info.detail.xhr.response + '_static')(requestConfig);
  if (toSwap === undefined) {
    info.preventDefault();
  }
});

htmx.defineExtension('static', {
  transformResponse: function(text, xhr, elt) {
    return toSwap;
  }
});
