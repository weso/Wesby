
function downloadAs(format) {
  var current = window.location.href;
  var extension = "." + format;
  window.location.href = current.replace(/\.[^\.]+$/, extension);
}