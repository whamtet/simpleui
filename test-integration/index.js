const puppeteer = require('puppeteer');

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  const page = await browser.newPage();

  await page.goto('http://localhost:3000/test/');

  page.click('#incrementer');
  const result = await page.waitForSelector('#result');
  console.log('result', result);

})();
