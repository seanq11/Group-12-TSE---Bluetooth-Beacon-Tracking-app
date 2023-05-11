using System.Collections.Generic;
using System.Threading;
using System.Text.Json;
using System;
using System.IO;

namespace TSE3
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            List<Coordinate> coordinates = new List<Coordinate>();

            bool done = false;

            while(done == false)
            {
                ReadJsonFile readJsonFile = new ReadJsonFile();
                coordinates.Add(readJsonFile.ReadJson());

                //when any key is pressed, the loop will end
                if(Console.KeyAvailable)
                {
                    done = true;
                }
            }

            //make a json file
            string json = JsonSerializer.Serialize(coordinates);
            //write the contents of the list of coordinates to the json file
            File.WriteAllText(@"./Coordinates.json", json);
        }
    }
}