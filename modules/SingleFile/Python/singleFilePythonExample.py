import sys

if not sys.argv.__len__ > 1:
    exit(1)
    
fileToOpen = sys.argv[1]
with open(fileToOpen, "w") as file:
    file.writelines("it works")
    
exit(0)