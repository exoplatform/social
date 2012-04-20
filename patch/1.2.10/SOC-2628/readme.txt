SOC-2628: Encode problem in experience profile

What is the problem to fix?
Encode problem in experience profile

Problem analysis
- Profile informations have been process(by StringEscapeUtils#escapeHtml(String) to ignore XSS attacking issue). So data will be ok when displayed on Browser but not ok when in edit mode of input boxes. So we must specify output cases and adapt, for example in edit mode of input boxes we must unescapeHtml before setting values.

How is the problem fixed?
- Use StringEscapeUtils.unescapeHtml() to unescape html characters.

Reproduction test
- Edit profile 
- Add experience 
- Input some special character à é è ç, etc
- Save -> OK
- Edit -> input field encoded -> KO
- could not save with error message -> KO
