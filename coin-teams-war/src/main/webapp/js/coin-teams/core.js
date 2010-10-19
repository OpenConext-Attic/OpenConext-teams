COIN.Core = function() {
	var moduleData = {}, debug = false;
	function callConsoleMethod(method, args) {
		if (typeof(console) !== 'undefined' && console != null) {
			console[method].apply(console, args);
		}
	}
	
	function createInstance(moduleId) {
		var instance = moduleData[moduleId].creator(COIN.Sandbox);
		if (!instance) {
			// @todo warn
			instance = {};
		}
		
		if (!debug) {
			var name, method;
			for (name in instance) {
				if (typeof method == "function") {
					instance[name] = function(name, method) {
						return function() {
							try {
								return method.apply(this, arguments);
							} catch(ex) {
								log(1, name + "(): " + ex.message);
							}
						}(name, method);
					}
				}
			}
		}
		return instance;
	}
	
	return {
		log: function() {
			callConsoleMethod('log', arguments)
		},
	
		warn: function() {
			callConsoleMethod('warn', arguments);
		},
		
		error: function() {
			callConsoleMethod('error', arguments);
		},
		
		register: function(moduleId, moduleCreator) {
			moduleData[moduleId] = {
					creator: moduleCreator,
					instance: null 
			};
		},

		start: function(moduleId) {
			var module = moduleData[moduleId]; 
			module.instance = createInstance(moduleId);
			
			if (typeof module.instance['init'] != 'undefined') {
				module.instance.init();
			}
			this.log("Started module: "+moduleId);
		},

		stop: function(moduleId) {
			var module = moduleData[moduleId]; 
			if (module.instance) {
				if (typeof module.instance['destroy'] != 'undefined') {
					module.instance.destroy(); 
				}
				module.instance = null;
			}
		},

		startAll: function() {
			for (var moduleId in moduleData) {
				if (moduleData.hasOwnProperty(moduleId)) { 
					this.start(moduleId);
				}
			}
		},

		stopAll: function() {
			for (var moduleId in moduleData) {
				if (moduleData.hasOwnProperty(moduleId)) { 
					this.stop(moduleId);
				}
			}
		}
	};
}();