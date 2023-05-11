using System.IO;
using System.Text.Json;

namespace TSE3
{
    internal class ReadJsonFile
    {
        public Coordinate ReadJson()
        {
            //read all text from a json file
            string text = File.ReadAllText(@"./Coordinate.json");
            Coordinate coordinate = JsonSerializer.Deserialize<Coordinate>(text);
            return coordinate;
        }
    }
}