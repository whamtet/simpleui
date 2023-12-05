const puppeteer = require('puppeteer');
const {assert} = require('chai');

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  const page = await browser.newPage();

  await page.goto('http://localhost:3000/test/');

  // warmup test

  page.click('#incrementer');
  const result = await page.waitForSelector('#result');
  assert.equal(await result.evaluate(e => e.innerHTML), '1');

  // path tests
  const innerHTML = selector => page.$eval(selector, e => e.innerHTML);

  assert.equal(await innerHTML('#path-check'), 'component_subcomponent');
  assert.equal(await innerHTML('#hash-check'), '#component_subcomponent');

  await page.$eval('#extra', e => e.value = 'hello');
  page.click('#component_subcomponent');

  const result2 = await page.waitForSelector('#result2');
  assert.equal(await result2.evaluate(e => e.value), 'hello');

  console.log('all tests passed');

  browser.close();

})();
