function closeModal() {
    var container = document.getElementById("modals-here")
    var backdrop = document.getElementById("modal-backdrop")
    var modal = document.getElementById("modal")

    modal.classList.remove("show")
    backdrop.classList.remove("show")

    setTimeout(function() {
        container.removeChild(backdrop)
        container.removeChild(modal)
    }, 200)
}