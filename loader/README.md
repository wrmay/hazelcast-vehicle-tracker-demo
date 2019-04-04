The loader is responsible for feeding entries into the system at a specified
multiple of the real rate as recorded.  We want the ordering amongst entries
for a single vehicle to be preserved but it is not necessary to completely
preserve the order between different vehicles.

There are 10,000 files, each a sequential record of GPS tracks.  Its probably
not a good idea to open 10k files at once.  There will be N double ended queues
of finite capacity.  Each file will be assigned to a queue according to a
"mod N" scheme.  Each queue will have a supplier thread responsible for filling
it.  To fill the queue, the background thread will open all of the files
assigned to that queue and  merge them in time order. This allows the files to
be processed without ever having to read the whole file into memory.  

On the other side of each queue will be a playback thread.  The playback thread
will "wake up" every second and feed X seconds worth of data into Hazelcast.

