function renderHeader(rdfType, rdfTypeLabel, cachedLabel) {
    var templateData = {
            "cachedLabel": cachedLabel,
            "rdfType": rdfType,
            "rdfTypeLabel": rdfTypeLabel
    };

    // Gets the Mustache template and renders the view
    $.get('@Options.host_r@routes.Assets.at("mustache/country.mustache")', function(template) {
        var output = Mustache.render(template, templateData);
        $('#renderedHeader').html(output);
    });
}