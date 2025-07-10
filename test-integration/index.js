const puppeteer = require('puppeteer');
const {assert} = require('chai');

const simpleUITests = async page => {
  await page.goto('http://localhost:3002/test/');

  // warmup test

  page.click('#incrementer');
  const result = await page.waitForSelector('#result');
  assert.equal(await result.evaluate(e => e.innerHTML), '1');

  // path tests
  const innerHTML = selector => page.$eval(selector, e => e.innerHTML);
  const value = selector => page.$eval(selector, e => e.value);

  assert.equal(await innerHTML('#i-check'), '0');
  assert.equal(await innerHTML('#index-check'), '0');
  assert.equal(await innerHTML('#path-check'), 'component_0_subcomponent');
  assert.equal(await innerHTML('#hash-check'), '#component_0_subcomponent');
  assert.equal(await value('#extra'), 'Matt');

  page.click('#component_0_subcomponent');

  await page.waitForSelector('#result2');
  assert.equal(await value('#result2'), 'Matt');

  // command tests
  page.click('#command-test');
  await page.waitForSelector('#result3');
  assert.equal(await innerHTML('#result3'), 'fuck');

};

const datastarTests = async page => {
  await page.goto('http://localhost:3002/test-datastar/');

  // warmup test

  page.click('#incrementer');
  const result = await page.waitForSelector('#result');
  assert.equal(await result.evaluate(e => e.innerHTML), '1');

  // path tests
  const innerHTML = selector => page.$eval(selector, e => e.innerHTML);
  const value = selector => page.$eval(selector, e => e.value);

  assert.equal(await innerHTML('#i-check'), '0');
  assert.equal(await innerHTML('#index-check'), '0');
  assert.equal(await innerHTML('#path-check'), 'component_0_subcomponent');
  assert.equal(await innerHTML('#hash-check'), '#component_0_subcomponent');
  assert.equal(await value('#extra'), 'Matt');

  page.click('#component_0_subcomponent');

  await page.waitForSelector('#result2');
  assert.equal(await value('#result2'), 'Matt');

  // command tests
  page.click('#command-test');
  await page.waitForSelector('#result3');
  assert.equal(await innerHTML('#result3'), 'fuck');

};

(async () => {
  try {

    // Launch the browser and open a new blank page
    const browser = await puppeteer.launch({headless: false});
    const page = await browser.newPage();

    await datastarTests(page);

    console.log('all tests passed');

    browser.close();

  } catch (e) {
    console.log(e);
  }

})();
