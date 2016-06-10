$(function () {
    $('.keyboard').keyboard({
        layout: 'custom',
        customLayout: {
            'default' : [
                // "n(a):title_or_tooltip"; n = new key, (a) = actual key, ":label" = title_or_tooltip (use an underscore "_" in place of a space " ")
                '\u0101 \u010D \u1E0D \u1E0F \u0113 \u01E7',
                '\u0121 \u1E25 \u1E96 \u1E2B \u0127 \u012B',
                '\u01F0 \u1E33 \u014D \u1E63 \u0161 \u1E6D',
                '\u1E6F \u016B \u0177 \u1E93 \u1E95 \u1D9C',
                '{shift} {accept} {cancel}'
            ],
            'shift' : [
                '\u0100 \u010C \u1E0C \u1E0E \u0112 \u01E6',
                '\u0120 \u1E24 H\u0331 \u1E2A \u0126 \u012A',
                'J\u030C \u1E32 \u014C \u1E62 \u0160 \u1E6C',
                '\u1E6E \u016A \u0176 \u1E92 \u1E94 \u1D9C',
                '{shift} {accept} {cancel}'
            ]
        },
        usePreview: false
    });
});
