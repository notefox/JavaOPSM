import sys

if __name__ == "__main__":
    if not sys.argv.__len__ > 1:
        exit(1)
    
    fileToOpen = sys.argv[1]
    with open(fileToOpen, "w") as file:
        file.write("it works")