let waiting;

const response = () => new Promise(resolve => waiting = resolve);

htmx.defineExtension('htmx-notify', {
	onEvent: function(name) {
		if (name === 'htmx:afterSettle') {
			if (waiting) {
				waiting();
			}
		}
	}
});
