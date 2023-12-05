const puppeteer = require('puppeteer');
const {assert} = require('chai');

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  const page = await browser.newPage();

  await page.goto('http://localhost:3000/test/');

  page.click('#incrementer');
  const result = await page.waitForSelector('#result');
  const innerHTML = await result.evaluate(e => e.innerHTML);

  assert.equal(innerHTML, '1');

  browser.close();

})();
