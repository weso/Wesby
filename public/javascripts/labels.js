


$(document).ready(function () {
    
    $('.triple-value').each(function () {
        $this = $(this);
        var uri = $this.find('a.value-uri').attr('href');
        var value = $this;
        // var labelDiv = $this.find('div.wesby-label');
        $.get('/wesby-label', {uri: uri})
            .done(function (label) {
                console.log(label);
                value.html('<a href="' + uri + '">' + label + '</a>');
            });
    });
});