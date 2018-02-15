
var Wesby = (function () {

  var downloadAs = function (format) {
    var current = window.location.href;
    var extension = "." + format;
    window.location.href = current.replace(/\.[^\.]+$/, extension);
  };

  var getContext = function (resource, cb) {
    // console.log('Getting ' + resource);
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


  var loadTemplate = function (dataLocation, resourceType) {

    $.ajax({
      method: "GET",
      url: dataLocation,
      accepts: {
        jsonld: 'application/ld+json'
      },
      cache: true
    }).done(function (data) {
      if (data.redirect) {
        // console.log("Redirecting to " + data.redirect);
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
            var templateName = templates[resourceType];
            if (!templateName) {templateName = 'default';}
            $.ajax({
              method: "GET",
              url: window.location.origin + '/assets/templates/' + templateName + '.hbs'
            }).done(function (source) {
              data['@context'] = ctx['@context'];
              if (data['label']) { document.title = data['label'] + ' - ' + document.title; }
              var template = Handlebars.compile(source);
              $('#result').append(template(data));
            });
          });
        });
      }
    });
  };

  var getSpinner = function() {
    return '<div class="loading">Cargando</div>';
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
  var morph = Metamorph(Wesby.getSpinner());
  // console.log(id);
  // console.log(options);
  Wesby.getContext(id, function (ctx) {
    morph.html('<a href="' + id + '">' + ctx[options.hash['textProp']][0] + '</a>');
    return ctx;
  });

  return new Handlebars.SafeString(morph.outerHTML());
});

Handlebars.registerHelper('img', function(id, options) {
    var morph = Metamorph(Wesby.getSpinner());

    Wesby.getContext(id, function (ctx) {
        // console.log(ctx['thumbnail'][0]);
        morph.html('<img class="thumbnail" ' +
            'src="' + ctx['thumbnail'][0] + '" ' +
            'alt="' + ctx['label'] + '" </img>');
        return ctx;
    });

    return new Handlebars.SafeString(morph.outerHTML());
});


// Concordance helpers
// ----------------------------------------------------------------------------
Handlebars.registerHelper('prevP', function(paragraphID) {
    var morph = Metamorph(Wesby.getSpinner());

    var paragraphName = paragraphID[0].split('/').pop();
    var paragraphNumber = Number(paragraphName.split('_').pop()) - 1;
    var book = paragraphID[0].split('/', 5)[4];
    var prevPID = window.location.origin + '/corpus/' + book + '/parrafo/par_' + pad(paragraphNumber, 6);

    Wesby.getContext(prevPID, function (ctx) {
        morph.html('<a href="' + prevPID + '">' + ctx['paragraphText'][0] + '</a>');
        return ctx;
    });

    return new Handlebars.SafeString(morph.outerHTML());
});

Handlebars.registerHelper('nextP', function(paragraphID) {
    var morph = Metamorph(Wesby.getSpinner());

    var paragraphName = paragraphID[0].split('/').pop();
    var paragraphNumber = Number(paragraphName.split('_').pop()) + 1;
    var book = paragraphID[0].split('/', 5)[4];
    var nextPID = window.location.origin + '/corpus/' + book + '/parrafo/par_' + pad(paragraphNumber, 6);

    Wesby.getContext(nextPID, function (ctx) {
        morph.html('<a href="' + nextPID + '">' + ctx['paragraphText'][0] + '</a>');
        return ctx;
    });

    return new Handlebars.SafeString(morph.outerHTML());
});

Handlebars.registerHelper('concordanceP', function(id, options) {
    var morph = Metamorph(Wesby.getSpinner());
    var concordanceCtx = this;
    // console.log(this);
    // console.log(options);
    Wesby.getContext(id, function (ctx) {
        var word = concordanceCtx.hasWord[0].split('/').pop();
        var text = ctx[options.hash['textProp']][0].replace(/echasen/ig, '<strong>' + word + '</strong>');
        morph.html('<a href="' + id + '">' + text + '</a>');
        return ctx;
    });

    return new Handlebars.SafeString(morph.outerHTML());
});

// Word helpers
// ----------------------------------------------------------------------------

Handlebars.registerHelper('book', function(paragraphId, options) {
  var morph = Metamorph(Wesby.getSpinner());

  Wesby.getContext(paragraphId, function (paragraph) {
    var bookId = paragraph['hasBook'];
    Wesby.getContext(bookId, function (book) {
      morph.html('<a href="bookId" >' + book['label'] + '</a>');
    });
  });

  return new Handlebars.SafeString(morph.outerHTML());
});

Handlebars.registerHelper('concordance', function (options) {
  var out = Metamorph('<td class="text-right concordance-left"></td><td class="text-center concordance-word">' + Wesby.getSpinner() + '</td><td class="text-left concordance-right"></td>');
  var concordance = options.hash.id;
  // console.log('Concordance: ' + concordance);
  // console.log(options.hash);
  var word = options.hash.word[0];

  Wesby.getContext(concordance, function (concordanceCtx) {
    Wesby.getContext(concordanceCtx.hasParagraph[0], function(paragraphCtx) {
      // TODO sólo coge el primer párrafo
      var text = paragraphCtx.paragraphText[0];
      var position = +concordanceCtx.position[0];
      var textL = text.substring(0, position);
      var textR = text.substring(position + word.length, text.length);
      var bookId = paragraphCtx['hasBook'];
      // var regex = new RegExp('\\b' + 'cantidad' + '\\b', 'ig');
      // out.html(text.replace(regex, '<strong>cantidad</strong>'));

      Wesby.getContext(bookId, function(book) {
        out.html(
            '<td><a href="' + bookId + '" >' + book['label'] + '</a></td>' +
            '<td class="text-right concordance-left">' + textL +
            '</td><td class="text-center concordance-word"><strong>'+ word +
            '</strong></td><td class="text-left concordance-right">'+ textR + '</td>'
        );
      });
      out.html(
          '<td>' + Wesby.getSpinner() + '</td>' +
          '<td class="text-right concordance-left">' + textL +
          '</td><td class="text-center concordance-word"><strong>'+ word +
          '</strong></td><td class="text-left concordance-right">'+ textR + '</td>'
      );
    });
  });

  return new Handlebars.SafeString(out.outerHTML());
});

// Work helpers
// ----------------------------------------------------------------------------

Handlebars.registerHelper('p', function(paragraph, options) {
  var out = Metamorph('<img src="' + window.location.origin + '/assets/images/loader.svg">');

  Wesby.getContext(paragraph, function (ctx) {
    out.html('<p>' + ctx.paragraphText[0] + '</p>');
  });

  return new Handlebars.SafeString(out.outerHTML());
});

// Util helpers
// ----------------------------------------------------------------------------
// Handlebars.registerHelper('loadMore', function (items, options) {
//   var out = Metamorph('<img src="' + window.location.origin + '/assets/images/loader.svg">');
//   var load = options.hash.load;
//   Wesby.pages = items;
//
//   var pages = items.slice(0, load);
//   var html = pages.forEach
//   out.html();
//
// });

// Register partials
// ----------------------------------------------------------------------------
// $.ajax({
//   method: "GET",
//   url: window.location.origin + '/assets/templates/partials/' + name + '.hbs'
// }).done(function (partial) {
//   Handlebars.registerPartial('pagination', partial);
// });

function pad(num, size) {
    var s = num + "";
    while (s.length < size) s = "0" + s;
    return s;
}