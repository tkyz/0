import os
import traceback
import re
import glob
import subprocess
import pathlib

class Main:

  class Blobfs:

    def __init__(self, base_dir):
      self.base_dir = os.path.abspath(base_dir)

    def ext(self, path):

      ext = None

      if re.fullmatch(self.base_dir + r'/.*\.([^/\.]+)',        path):
        ext = re.sub( self.base_dir + r'/.*\.([^/\.]+)', r'\1', path).lower()
        ext = ext.lower()

      return ext

    def mime_type(self, path):

      ret = subprocess.run(['file', '--brief', '--mime-type', path], capture_output = True, text = True)
      if 0 == ret.returncode:

        mime_type = re.sub(r'[\r\n]+', '', ret.stdout)
        mime_type = mime_type.lower()

        blob_dir = self.base_dir + '/ref/mime_type/' + mime_type
        os.makedirs(blob_dir, exist_ok=True)

      return mime_type

    def run(self):

      sub_dirs = []
      sub_dirs.append('/blob/sha256')
      sub_dirs.append('/ref')

      for sub_dir in sub_dirs:
        for path in glob.iglob(self.base_dir + sub_dir + '/**/*', recursive=True):

          if not os.path.isfile(path):
            continue
          if os.path.islink(path):
            continue
          if re.fullmatch(self.base_dir + r'/blob/sha256/([0-9a-f]{2}/)*([0-9a-f]{64})\.tmp/.*', path):
            continue

          st_nlink  = os.stat(path).st_nlink
          ext       = self.ext(path)
          mime_type = self.mime_type(path)

          if mime_type == 'inode/x-empty':
            continue
          if mime_type.startswith('text'):
            continue

#         if 1 == st_nlink:
#           print(str(st_nlink) + ' ' + str(ext) + ' ' + path)

          print(str(st_nlink) + ' ' + str(mime_type) + ' ' + str(ext) + ' ' + path)

  def run():
    try:
      Main.Blobfs().run()
    except:
      traceback.print_exc()

if __name__ == '__main__':
  Main.run()
