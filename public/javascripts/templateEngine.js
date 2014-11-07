/**
 * Client side of the template engine.
 */

var template = "test"; // Mustache template
var templateName;
var data = {}; // Mustache server data
var partials = {}; // Template partials

var TemplateEngine = function() {
    console.log("Template engine started");
};

TemplateEngine.prototype.render = function() {
    console.log("Rendering page");

    /**
     * Wait for AJAX calls to complete, then render the page.
     */
    $(document).ajaxStop(function(){
        var renderedPage = Mustache.render(template, data, partials);
        $('#mustacheRendered').html(renderedPage);
    });
};

TemplateEngine.prototype.loadData = function(callback) {
    console.log("Loading data");

    $.getJSON(WESBY.routes.jsonService, function (d) {
        $.extend(data, d);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR);
        console.log(textStatus);
        console.log(errorThrown);
    })
    .done(function(textStatus) {
        callback("success");
    });
};

TemplateEngine.prototype.loadPartials = function(callback) {
    console.log("Loading partials");

    $.getJSON(WESBY.routes.templates + "partials/partials.json", function(p) {

        p.forEach(function(partialName){
            $.get(WESBY.routes.templates + "partials/" + partialName +'.mustache', function(t) {

                partials[partialName] = t;
            });
        });

    })
    .fail(function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR);
        console.log(textStatus);
        console.log(errorThrown);
    })
    .done(function(textStatus) {
        callback("success");
    });
};

TemplateEngine.prototype.loadTemplateNames = function(callback) {
    console.log("Loading template names");

    $.getJSON(WESBY.routes.templatesMap, function(templatesMap) {
        templateName = templatesMap[data.rdfType];
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR);
        console.log(textStatus);
        console.log(errorThrown);
    })
    .done(function(textStatus) {
        callback("success");
    });
};

TemplateEngine.prototype.loadTemplate = function(callback) {
    $.get(WESBY.routes.templates + templateName + '.mustache', function(t) {
        template = t;
    }).fail(function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR);
        console.log(textStatus);
        console.log(errorThrown);
    })
    .done(function(textStatus) {
        callback("success");
    });
};




