var ratings = document.querySelectorAll('.rating');
var total = document.querySelector('.total');

function onRatingChange() {
    let totalValue = 0;
    for (rating of ratings) {
        var value = rating.value;
        if (value) {
            totalValue += new Number(rating.value);
        }
    }
    total.textContent = totalValue;
};

ratings.forEach(function(e) {
    e.addEventListener('change', onRatingChange);
});
