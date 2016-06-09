
var Wesby = (function () {

  var downloadAs = function (format) {
    var current = window.location.href;
    var extension = "." + format;
    window.location.href = current.replace(/\.[^\.]+$/, extension);
  };

  var getContext = function (resource, cb) {
    $.ajax({
      method: "GET",
      url: resource + '.jsonld',
      accepts: {
        jsonld: 'application/ld+json'
      }
    }).done(function (data) {
      cb(data);
    });
  };


  var loadTemplate = function (dataLocation) {

    $.ajax({
      method: "GET",
      url: dataLocation,
      accepts: {
        jsonld: 'application/ld+json'
      },
      cache: true
    }).done(function (data) {
      if (data.redirect) {
        console.log("Redirecting to " + data.redirect);
        this.loadTemplate(data.redirect);
      } else {
        $.ajax({
          method: "GET",
          url: "/context"
        }).done(function (ctx) {
          $.ajax({
            method: "GET",
            url: window.location.origin + '/assets/templates/templates.json'
          }).done(function (templates) {
            var templateName = templates[data.type[0]];
            if (!templateName) {templateName = 'default';}
            $.ajax({
              method: "GET",
              url: window.location.origin + '/assets/templates/' + templateName + '.hbs'
            }).done(function (source) {
              data['@context'] = ctx['@context'];
              var template = Handlebars.compile(source);
              $('#result').append(template(data));
            });
          });
        });
      }
    });
  };

  var getSpinner = function() {
    return '<img src="' + window.location.origin + '/assets/images/loader.svg">';
    // return 'Loading...';
  };

  return {
    downloadAs: downloadAs,
    getContext: getContext,
    loadTemplate: loadTemplate,
    getSpinner: getSpinner
  }
})();

Handlebars.registerHelper('graph', function(ctx) {
  return ctx['@graph'];
});

Handlebars.registerHelper('id', function(ctx) {
  return ctx['@id'];
});

Handlebars.registerHelper('a', function(id, options) {
  var morph = Metamorph('<img src="' + window.location.origin + '/assets/images/loader.svg">');

  Wesby.getContext(id, function (ctx) {
    morph.html('<a href="' + id + '">' + ctx[options.hash['textProp']][0] + '</a>');
    return ctx;
  });

  return new Handlebars.SafeString(morph.outerHTML());
});

// Word helpers
// ----------------------------------------------------------------------------

Handlebars.registerHelper('wordInParagraph', function (concordance, options) {
  var out = Metamorph(Wesby.getSpinner());

  Wesby.getContext(concordance, function (concordanceCtx) {
    Wesby.getContext(concordanceCtx.hasParagraph, function(paragraphCtx) {
      var text = paragraphCtx.paragraphText[0];
      var position = +concordanceCtx.position[0];
      var textL = text.substring(0, position);
      var textR = text.substring(position + 'cantidad'.length, text.length);
      // var regex = new RegExp('\\b' + 'cantidad' + '\\b', 'ig');
      // out.html(text.replace(regex, '<strong>cantidad</strong>'));
      out.html(textL + ' <strong>cantidad</strong> ' + textR);
    });
  });

  return new Handlebars.SafeString(out.outerHTML());
});
