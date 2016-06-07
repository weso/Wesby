
var Wesby = (function () {

  var downloadAs = function (format) {
    var current = window.location.href;
    var extension = "." + format;
    window.location.href = current.replace(/\.[^\.]+$/, extension);
  };

  var getContext = function (uri, cb) {
    $.ajax({
      method: "GET",
      url: uri,
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
            url: 'http://localhost:9000/assets/templates/templates.json'
          }).done(function (templates) {
            var templateName = templates[data.type[0]];
            if (!templateName) {templateName = 'default';}
            $.ajax({
              method: "GET",
              url: 'http://localhost:9000/assets/templates/' + templateName + '.hbs'
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

  return {
    downloadAs: downloadAs,
    getContext: getContext,
    loadTemplate: loadTemplate
  }
})();

Handlebars.registerHelper('graph', function(ctx) {
  return ctx['@graph'];
});

Handlebars.registerHelper('id', function(ctx) {
  return ctx['@id'];
});

Handlebars.registerHelper('a', function(id, options) {
  var morph = Metamorph('<img src="http://localhost:9000/assets/images/loader.svg">');
  console.log(options.hash['textProp']);

  Wesby.getContext(id + '.jsonld', function (ctx) {
    morph.html('<a href="' + id + '">' + ctx[options.hash['textProp']][0] + '</a>');
    return ctx;
  });

  return new Handlebars.SafeString(morph.outerHTML());
});
