const oldLoad = onload;
onload = () => {
    if (oldLoad) {
        oldLoad();
    }
    const content = htmx.find('#to-sort');
    new Sortable(content, {animation: 150, ghostClass: 'blue-background-class'});
};