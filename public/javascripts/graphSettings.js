var GraphSettings = new (function() {
	this.get = function() {
		return {
			"width": 600,
			"height": 400,
			"margins": [10, 20, 10, 1],
			"backgroundColour": "none",
			"serieColours": ["#116699", "#88CC22", "#CC2222", "#EEAA11", "#333366", "#006644", "#882222", "#EEAA11"],
			"overColour": "#333",
			"groupMargin": 4,
			"barMargin": 8,
			"valueOnItem": {
				"show": false,
				"margin": 5,
				"font-family": "Helvetica",
				"font-colour": "#666",
				"font-size": "16px",
			},
			"xAxis": {
				"title": "Years",
				"colour": "none",
				"margin": 10,
				"tickColour": "none",
				"values": [],
				"font-family": "Helvetica",
				"font-colour": "#666",
				"font-size": "16px"
			},
			"yAxis": {
				"title": "Values",
				"colour": "none",
				"margin": 5,
				"tickColour": "#ddd",
				"font-family": "Helvetica",
				"font-colour": "#aaa",
				"font-size": "16px"
			},
			"series": [],
			"legend": {
			    "show": true,
			    "itemSize": 1,
			    "font-colour": "#666",
				"font-family": "Helvetica",
				"font-size": "16px"
			}
		}
	}
})();