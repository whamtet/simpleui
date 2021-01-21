htmx.config.defaultSettleDelay = 0;
htmx.config.defaultSwapStyle = 'outerHTML';

var params;
var toSwap;

document.body.addEventListener('htmx:beforeRequest', function(info) {
  params = info.detail.requestConfig.parameters;
});

document.body.addEventListener('htmx:beforeSwap', function(info) {
  const xhr = info.detail.xhr;
  toSwap = eval(xhr.response)(params);
  if (toSwap === undefined) {
    info.preventDefault();
  }
});

htmx.defineExtension('my-ext', {
  transformResponse: function(text, xhr, elt) {
    return toSwap;
  }
});
