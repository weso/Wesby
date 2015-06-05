function SortedArray() {
    var index = 0;
    this.array = [];
    var length = arguments.length;
    
    while (index < length) 
    	this.insert(arguments[index++]);
    
	this.length = 0;
	
	this.insert = function (element) {
	    var array = this.array;
	    var index = array.length;
	    array.push(element);
	    
	    this.length = this.array.length;
	
	    while (index) {
	        var i = index, j = --index;
	
	        if (array[i] < array[j]) {
	            var temp = array[i];
	            array[i] = array[j];
	            array[j] = temp;
	        }
	    }
	
	    return this;
	};
	
	this.get = function (index) {
		return this.array[index];
	};
	
	this.getArray = function() {
		return this.array;	
	};
	
	// No repeated elements allowed
	
	this.uniqueInsert = function (element) {
		if (this.search(element) != -1)
			return;
			
		this.insert(element);
	}
	
	this.search = function (element) {
	    var low = 0;
	    var array = this.array;
	    var high = array.length;
	
	    while (high > low) {
	        var index = (high + low) / 2 >>> 0;
	        var cursor = array[index];
	
	        if (cursor < element) low = index + 1;
	        else if (cursor > element) high = index;
	        else return index;
	    }
	
	    return -1;
	};
	
	this.remove = function (element) {
	    var index = this.search(element);
	    if (index >= 0) this.array.splice(index, 1);
	    
	    this.length = this.array.length;
	    
	    return this;
	};
}