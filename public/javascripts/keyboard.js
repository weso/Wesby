$(function () {
    $('.keyboard').keyboard({
        layout: 'custom',
        customLayout: {
            'default' : [
                // "n(a):title_or_tooltip"; n = new key, (a) = actual key, ":label" = title_or_tooltip (use an underscore "_" in place of a space " ")
                // a c c d d e
                '\u0101 \u010D \u010B \u1E0D \u1E0F \u0113',
                // g g h h h h
                '\u01E7 \u0121 \u1E25 \u1E96 \u1E2B \u0127',
                // i j k o s s
                '\u012B \u01F0 \u1E33 \u014D \u1E63 \u0161',
                // t t u y z z
                '\u1E6D \u1E6F \u016B \u0177 \u1E93 \u1E95',
                // a c h i
                '\u1D43 \u1D9C \u02B0 \u1DA5' ,
                // i n t u
                '\u02E1 \u207F \u1D57 \u1D58',
                '{shift} {cancel}'
            ],
            'shift' : [
                // a c _ d d e
                '\u0100 \u010C {sp:1} \u1E0C \u1E0E \u0112',
                // g g h h h h
                '\u01E6 \u0120 \u1E24 H\u0331 \u1E2A \u0126',
                // i j k o s s
                '\u012A J\u030C \u1E32 \u014C \u1E62 \u0160',
                // t t u y z z
                '\u1E6C \u1E6E \u016A \u0176 \u1E92 \u1E94',
                // a c h i
                '\u1D43 \u1D9C \u02B0 \u1DA5' ,
                // i n t u
                '\u02E1 \u207F \u1D57 \u1D58',
                '{shift}  {cancel}'
            ]
        },
        usePreview: false,
        display: {
            'shift': 'Mayus',
            'cancel': 'Cancelar'
        }
    });
});
