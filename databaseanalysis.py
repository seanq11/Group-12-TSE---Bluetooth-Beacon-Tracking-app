# Libraries
import customtkinter # pip install customtkinter
import tkinter
import numpy as np # pip install numpy
import matplotlib.pyplot as plt # pip install matplotlib
import sqlite3

from tkinter import Pack
from tkinter import Tk
from tkinter.filedialog import askopenfilename
from sklearn.cluster import KMeans # pip install scikit-learn
from sklearn.preprocessing import StandardScaler
from collections import Counter


# Functions
def open_file():
    global filename  # Access the global variable
    Tk().withdraw()
    filename = askopenfilename()
    file_label.configure(text=f"Selected database: {filename}")
    print(filename)
    return filename

def connect_to_database():
    global filename  # Access the global filename variable
    global coordinates # Access the global coordinates variable

    conn = sqlite3.connect(filename) # Connect to .db database

    with open('sql_coordinates.sql', 'w') as f: # Generate sql_coordinates.sql file and write contents of .db file to it
        for line in conn.iterdump():
            f.write(f"{line}\n")

    #f.close()
    #conn.close()

    cursor = conn.execute("SELECT x, y FROM coordinates") # Subquery to select only x and y coordinates
    data = cursor.fetchall()
    coordinates = np.array(data) # Insert coordinate data to numpy array
    print(coordinates)


    algorithm()

def algorithm():
    ##########################################
    ##     TESTING OF K-MEANS ALGORITHM     ##
    ##########################################

    #scaler = StandardScaler()
    #X_scaled = scaler.fit_transform(X)
    #sse = []
    #for k in range(1, 11):
    #    kmeans = KMeans(n_clusters=k, random_state=0)
    #    kmeans.fit(X_scaled)
    #    sse.append(kmeans.inertia_)
    #kmeans = KMeans(n_clusters=10, random_state=0)
    #kmeans.fit(X_scaled)
    #centroid = np.mean(X, axis=0)
    #cluster_centers = scaler.inverse_transform(kmeans.cluster_centers_)
    #closest_cluster_center = cluster_centers[np.argmin(np.linalg.norm(cluster_centers - centroid, axis=1))]
    #closest_cluster_indices = np.where(np.all(cluster_centers == closest_cluster_center, axis=1))[0]
    #most_accessed_coords = X[closest_cluster_indices]
    #output_text.insert(tkinter.END, f"Clusters: {k}\n")
    #output_text.insert(tkinter.END, f"Most accessed area: {most_accessed_coords}\n\n")

    X = coordinates # Defines x as coordinates
    coordinate_tuples = [tuple(coord) for coord in coordinates]
    most_common_coords = Counter(coordinate_tuples).most_common(10) # Only show 10 coordinates
    output_text.insert(tkinter.END, f"TOP 10 Most commonly accessed areas/coordinates: \n")
    for coord, count in most_common_coords: # For each coordinate...
        output_text.insert(tkinter.END, f"{coord} - Count: {count}\n") # Output the coordinate and frequency of it
    heatmap()

def heatmap():
    # Create a 2D histogram of the coordinates
    xbins = np.linspace(-100, 100, 50) # X coordinates
    ybins = np.linspace(-100, 100, 50) # Y coordinates
    hist, _, _ = np.histogram2d(coordinates[:,0], coordinates[:,1], bins=[xbins, ybins])

    # Normalize the histogram so that the color of each bin is determined by its frequency
    norm = plt.Normalize(vmin=hist.min(), vmax=hist.max())
    cmap = plt.get_cmap('Reds') # Red heats

    # Plot the heatmap
    fig, ax = plt.subplots(figsize=(8,8))
    im = ax.imshow(hist.T, cmap=cmap, norm=norm, origin='lower', extent=[xbins[0], xbins[-1], ybins[0], ybins[-1]])

    # Add colorbar to the plot
    cbar = fig.colorbar(im, ax=ax, fraction=0.046, pad=0.04) # Colour gradient of heatmap

    # Add labels and title
    ax.set_xlabel('X Coordinate (m)')
    ax.set_ylabel('Y Coordinate (m)')
    ax.set_title('Heatmap of All Coordinates')

    plt.show() # Displays the heatmap window


# Variables
filename = ""


# UI
customtkinter.set_appearance_mode("dark") # Dark mode
customtkinter.set_default_color_theme("dark-blue") # Dark blue colour accents

root = customtkinter.CTk()
root.geometry("1280x720") # Size of window

frame = customtkinter.CTkFrame(master=root)
frame.pack(fill="both", expand=True)

import_file = customtkinter.CTkButton(master=root, text="Choose database from system\n(MUST BE '.sql' FILE FORMAT)", command=open_file, width=200, height=60) # Import file button
import_file.place(relx=0.41, rely=0.1, anchor=tkinter.CENTER)

file_label = customtkinter.CTkLabel(master=root, text="No database selected.", width=600, height=60) # Label, which states what file is selected
file_label.place(relx=0.5, rely=0.2, anchor=tkinter.CENTER)

output_text = tkinter.Text(master=root, width=720, height=420) # Text field, which gives user all the outputs
output_text.place(relx=0.5, rely=0.6, anchor=tkinter.CENTER, width=720, height=420)
output_text.config(font=("Courier", 18))

connect = customtkinter.CTkButton(master=root, text="Analyse the database", command=connect_to_database, width=200, height=60) # Button which makes database connect to the program
connect.place(relx=0.59, rely=0.1, anchor=tkinter.CENTER)

root.mainloop()

