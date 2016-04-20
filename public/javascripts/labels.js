


$(document).ready(function () {
    
    $('.triple-value').each(function () {
        $this = $(this);
        var uri = $this.find('a.value-uri').attr('href');
        var value = $this;
        // var labelDiv = $this.find('div.wesby-label');
        if (uri) {
            $.get('/wesby-label', {uri: uri})
                .done(function (d) {
                    if (d.status === "success") {
                        value.html('<a href="' + uri + '">' + d.data.label + '</a>');
                    }
                });
        }
    });
});