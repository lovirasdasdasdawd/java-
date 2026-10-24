"""Create original, deterministic surf textures with periodic DSP, not sampled recordings.
Requires numpy, scipy and ffmpeg. Output: three 24-second, mono Vorbis loops.
"""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess,tempfile
RATE=22050;SECONDS=24;N=RATE*SECONDS
out=Path(__file__).resolve().parents[1]/'fabric/src/main/resources/assets/donuttracker/sounds/ocean'
out.mkdir(parents=True,exist_ok=True)
t=np.arange(N)/N;freq=np.fft.rfftfreq(N,1/RATE)
def noise(rng,low,high,tilt):
    spectrum=rng.normal(size=len(freq))+1j*rng.normal(size=len(freq))
    response=(1-np.exp(-(freq/max(low,1))**2))*np.exp(-(freq/high)**2)/np.maximum(freq,20)**tilt
    spectrum*=response;spectrum[0]=0;spectrum[-1]=spectrum[-1].real
    result=np.fft.irfft(spectrum,n=N);return result/np.std(result)
for index,(name,cycles,rumble,foam,level) in enumerate([('shoreline',3,340,4000,.11),('open_sea',2,210,2600,.14),('rainy_coast',4,300,7000,.12)]):
    rng=np.random.default_rng(7301+index)
    swell=(.5+.5*np.sin(2*np.pi*cycles*t+.6*np.sin(2*np.pi*t)))**2
    rolling=noise(rng,35,rumble,.42)*(.3+.7*swell)
    wash=noise(rng,180,foam,.23)*(.12+.88*swell)**1.4
    signal=.65*rolling+.4*wash
    if name=='rainy_coast':signal+=.25*noise(rng,700,7500,.08)*(.75+.25*np.sin(2*np.pi*t))
    # Smooth deterministic modulation and periodic FFT noise make a seamless loop.
    signal*=level
    signal/=max(1,np.max(np.abs(signal))/.65)
    assert np.isfinite(signal).all() and np.max(np.abs(signal))<.66
    with tempfile.TemporaryDirectory() as temp:
        wav=Path(temp)/'loop.wav';wavfile.write(wav,RATE,(signal*32767).astype(np.int16))
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out/(name+'.ogg'))],check=True)
    print(name,'RMS',round(float(np.sqrt(np.mean(signal**2))),4),'peak',round(float(np.max(np.abs(signal))),4))
