var richTable = new Object();

////////////////////////////////////////////////////////////////
// Pages
////////////////////////////////////////////////////////////////

richTable.pages = new (function() {
	var rowsPerPage = 60;
	var firstShownRow = 0;

	(function init() {
		var tables = document.querySelectorAll("table.pages");
		var length = tables.length;
		
		for (var i = 0; i < length; i++)
			prepareTable(tables[i]);
	})();
	
	function prepareTable(table) {
		table.isPaged = true;
		table.rowsPerPage = rowsPerPage;
		table.firstShownRow = firstShownRow;
		
		var size = showPage(table);
		table.numberOfRows = size.numberOfRows;
		table.numberOfColumns = size.numberOfColumns;
		table.numberOfPages = Math.ceil(size.numberOfRows / table.rowsPerPage);
		table.selectedPage = 1;
		
		table.changePage = changePage;
		table.reloadPage = function () {
			showPage(this);
		}
		
		// Footer
		createFooter(table);
	}
	
	function changePage(pageNumber) {
		if (pageNumber > this.numberOfPages || pageNumber < 1)
			return;
			
		this.firstShownRow = (pageNumber - 1) * this.rowsPerPage;
		this.selectedPage = pageNumber;
		
		showPage(this);
	}
	
	function showPage(table) {
		var firstShownRow = table.firstShownRow;
		var rowsPerPage = table.rowsPerPage;
	
		var tBodies = table.tBodies;
		var length = tBodies.length;
		
		var count = 0;
		var lastShownRow = firstShownRow + rowsPerPage - 1;
		
		var numberOfColumns = 0;
		
		for (var i = 0; i < length; i++) {
			var rows = tBodies[i].rows;
			var rowNumber = rows.length;
			
			for (var j = 0; j < rowNumber; j++) {
				var shown = count >= firstShownRow && count <= lastShownRow;
				rows[j].style.display = shown == true ? "table-row" : "none";
				
				if (rows[j].cells.length > numberOfColumns)
					numberOfColumns = rows[j].cells.length;
				
				count++;
			}
		}
		
		// Set footer link as selected
		updateFooter(table);
		
		return  {
			numberOfRows: count,
			numberOfColumns: numberOfColumns
		}
	}
	
	function updateFooter(table) {
		var selectedPage = table.selectedPage;
		var numberOfPages = table.numberOfPages;
		
		// First link
		var link = table.querySelector("tFoot a.first");
		
		if (link) {
			link.className = selectedPage > 1 ? "not-number first" : "not-number first disabled";
			link.index = selectedPage > 1 ? 1 : -1;
		}
		
		// Previous link
		var link = table.querySelector("tFoot a.previous");
		
		if (link) {
			link.className = selectedPage > 1 ? "not-number previous" : "not-number previous disabled";
			link.index = selectedPage > 1 ? selectedPage - 1 : -1;		
		}
		
		// Next link
		var link = table.querySelector("tFoot a.next");
		
		if (link) {
			link.className = selectedPage < numberOfPages ? "not-number next" : "not-number next disabled";
			link.index = selectedPage < numberOfPages ? selectedPage + 1 : -1;	
		}
		
		// Last link
		var link = table.querySelector("tFoot a.last");
		
		if (link) {
			link.className = selectedPage < numberOfPages ? "not-number last" : "not-number last disabled";
			link.index = selectedPage < numberOfPages ? numberOfPages : -1;		
		}
		
		// Number links
		var links = table.querySelectorAll("tFoot a.number");
		var length = links.length;
		
		for (var i = 1; i <= length; i++)
			links[i - 1].className = i == selectedPage ? "number selected" : "number";
	}
	
	function createFooter(table) {
		var footer = document.createElement("tFoot");
		
		table.appendChild(footer);
		table.navFooter = footer;
		
		var tr = document.createElement("tr");
		footer.appendChild(tr);
		
		var td = document.createElement("td");
		tr.appendChild(td);
		td.colSpan = table.numberOfColumns;
		
		// First link
		var a = createLink(table, "<<", "not-number first disabled", -1);
		td.appendChild(a);
		
		// Previous link
		var a = createLink(table, "<", "not-number previous disabled", -1);
		td.appendChild(a);
		
		// Number links
		var length = table.numberOfPages;
		
		for (var i = 1; i <= length; i++) {
			var a = createLink(table, i, i == 1 ? "number selected" : "number", i);
			td.appendChild(a);
		}
		
		// Next link
		var a = createLink(table, ">", length > 1 ? "not-number next" : "not-number next disabled", length > 1 ? 2 : -1);
		td.appendChild(a);		
		
		// Last link
		var a = createLink(table, ">>", length > 1 ? "not-number last" : "not-number last disabled", length > 1 ? length : -1);
		td.appendChild(a);
	}
	
	function createLink(table, text, className, index) {
		var a = document.createElement("a");
		a.className = className;
		a.table = table;
		a.index = index;
		
		a.onclick = function() {
			this.table.changePage(this.index);
		};
		
		var textNode = document.createTextNode(text);
		a.appendChild(textNode);
		
		return a;
	}
})();

////////////////////////////////////////////////////////////////
// Sortable 
////////////////////////////////////////////////////////////////

richTable.sort = new (function() {
	(function init() {
		var tables = document.querySelectorAll("table.sortable");
		var length = tables.length;
		
		for (var i = 0; i < length; i++)
			iterateOverRows(tables[i]);
	})();
	
	function iterateOverRows(table) {
		var headers = table.tHead.rows;
		var length = headers.length;
	
		if (length > 0) {
			headers = headers[0].cells;
			length = headers.length;
	
			for (var i = 0; i < length; i++) {
				headers[i].table = table;
				headers[i].index = i;
				headers[i].sort = "empty";
				
				var img = new Image();
				img.src = "empty.png";
				headers[i].appendChild(img);
				
				headers[i].onclick = function() {
					sortRows(this.table, this.index, this);
				};
			}
		}
	}
	
	function sortRows(table, index, header) {
		// Insert rows in array
		
		var rows = new Array();
		
		var length = table.tBodies.length;
		
		for (var i = 0; i < length; i++) {
			var tBody = table.tBodies[i];
		
			for (var j = 0; j < tBody.rows.length; j++) {
				var row = tBody.rows[j];
			
				rows.push(row);
			}
		}
		
		// Sorting
		
		if (header.sort == "empty" || header.sort == "down")
			header.sort = "up";
		else
			header.sort = "down";	
		
		rows.sort(function(row1, row2) {
			var row1 = row1.cells[index].innerHTML.toString();
			var row2 = row2.cells[index].innerHTML.toString();	
		
			if (row1 < row2)
				return header.sort == "up" ? - 1 : 1;
			else if (row1 > row2)
				return header.sort == "up" ? 1 : -1;
			else
				return 0;
		});
		
		// Update header image
		var img = header.querySelectorAll("img");
		img[0].src = header.sort + ".png";
		
		// Update header image for the rest of columns
		var tHeader = table.tHead.rows[0].cells;
		var length = tHeader.length;
		
		for (var i = 0; i < length; i++)
			if (i != index)
				emptyHeaderSorter(tHeader[i]);
		
		// Delete rows
		var tHead = table.tHead;
		var tFoot = table.tFoot;
		
		while(table.hasChildNodes())
			table.removeChild(table.firstChild);
			
		// Insert head
		table.appendChild(tHead);
		
		// Insert footer
		table.appendChild(tFoot);
		
		// Insert sorted rows
		var tBody = document.createElement('tbody');
		table.appendChild(tBody);
		
		var length = rows.length;
		
		for (var i = 0; i < length; i++)
			tBody.appendChild(rows[i]);
			
		// If table is paged then it must be reloaded
		// If has reloadPage method then it is paged
		if (table.reloadPage)	
			table.reloadPage();
	}
	
	function emptyHeaderSorter(header) {
		var img = header.querySelectorAll("img");
		
		if (img && img.length && img.length > 0)
			img[0].src = "empty.png";
			
		header.sort = "empty";
	}
})();