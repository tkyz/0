import os
import traceback
import pprint
import re
import glob

class Main:

  def walk():

    type_cnt = {}

    for path in glob.iglob('./**/*', recursive=True):

      if os.path.isdir(path):
        continue
      if os.path.islink(path):
        continue

      st_nlink  = os.stat(path).st_nlink
      ext       = None
      mime_type = None

      if re.fullmatch(r'.*\.([^/\.]+)', path):
        ext = re.sub( r'.*\.([^/\.]+)', r'\1', path).lower()

      # TODO: mime_type
      if False:
        pass

      if ext is None:
        continue
      if ext.startswith('test_'):
        continue
      if ext in ['license', 'template']:
        continue

      cnt = type_cnt.get(ext)
      cnt = cnt if cnt is not None else 0
      type_cnt[ext] = cnt + 1

    pprint.pprint(type_cnt)

  def run():
    try:
      Main.walk()
    except:
      traceback.print_exc()

if __name__ == '__main__':
  Main.run()
